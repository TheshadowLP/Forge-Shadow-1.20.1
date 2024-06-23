package net.shadowbeast.arcanemysteries.temprature.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.config.ConfigSettings;
import net.shadowbeast.arcanemysteries.registries.BlockTempRegistry;
import net.shadowbeast.arcanemysteries.temprature.Temperature;
import net.shadowbeast.arcanemysteries.temprature.temp.BlockTemp;
import net.shadowbeast.arcanemysteries.util.MathHelper;
import net.shadowbeast.arcanemysteries.util.WorldHelper;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.Function;

public class BlockTempModifier extends TempModifier
{
    public BlockTempModifier() {}

    public BlockTempModifier(int range)
    {   this.getNBT().putInt("RangeOverride", range);
    }

    Map<ChunkPos, ChunkAccess> chunks = new HashMap<>(16);

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        Map<BlockTemp, Double> affectMap = new HashMap<>(128);
        Map<BlockPos, BlockState> stateCache = new HashMap<>(4096);
        List<Triplet<BlockPos, BlockTemp, Double>> triggers = new ArrayList<>(128);

        Level level = entity.level();
        int range = this.getNBT().contains("RangeOverride", 3) ? this.getNBT().getInt("RangeOverride") : ConfigSettings.BLOCK_RANGE.get();

        int entX = entity.blockPosition().getX();
        int entY = entity.blockPosition().getY();
        int entZ = entity.blockPosition().getZ();
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
        int minBuildHeight = level.getMinBuildHeight();

        boolean shouldTickAdvancements = this.getTicksExisted() % 20 == 0;

        for (int x = -range; x < range; x++)
        {
            for (int z = -range; z < range; z++)
            {
                ChunkPos chunkPos = new ChunkPos((entX + x) >> 4, (entZ + z) >> 4);
                ChunkAccess chunk = chunks.get(chunkPos);
                if (chunk == null) chunks.put(chunkPos, chunk = WorldHelper.getChunk(level, chunkPos));
                if (chunk == null) continue;

                for (int y = -range; y < range; y++)
                {
                    try
                    {
                        blockpos.set(entX + x, entY + y, entZ + z);

                        BlockState state = stateCache.get(blockpos);
                        if (state == null)
                        {   LevelChunkSection section = WorldHelper.getChunkSection(chunk, blockpos.getY());
                            state = section.getBlockState(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
                            stateCache.put(blockpos.immutable(), state);
                        }

                        if (state.isAir()) continue;

                        // Get the BlockTemp associated with the block
                        Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(state);

                        if (blockTemps.isEmpty() || (blockTemps.size() == 1 && blockTemps.contains(BlockTempRegistry.DEFAULT_BLOCK_TEMP))) continue;

                        // Get the amount that this block has affected the player so far

                        // Is totalTemp within the bounds of any BlockTemp's min/max range?
                        boolean isInTempRange = affectMap.isEmpty();
                        if (!isInTempRange)
                        {   for (Map.Entry<BlockTemp, Double> entry : affectMap.entrySet())
                        {   BlockTemp key = entry.getKey();
                            Double value = entry.getValue();

                            if (!blockTemps.contains(key) || MathHelper.betweenInclusive(value, key.minEffect(), key.maxEffect()))
                            {   isInTempRange = true;
                                break;
                            }
                        }
                        }
                        if (isInTempRange)
                        {
                            // Get Vector positions of the centers of the source block and player
                            Vec3 pos = Vec3.atCenterOf(blockpos);

                            // Gets the closest point in the player's BB to the block
                            double playerRadius = entity.getBbWidth() / 2;
                            Vec3 playerClosest = new Vec3(MathHelper.clamp(pos.x, entity.getX() - playerRadius, entity.getX() + playerRadius),
                                    MathHelper.clamp(pos.y, entity.getY(), entity.getY() + entity.getBbHeight()),
                                    MathHelper.clamp(pos.z, entity.getZ() - playerRadius, entity.getZ() + playerRadius));

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            int[] blocks = new int[1];
                            Vec3 ray = pos.subtract(playerClosest);
                            Direction direction = Direction.getNearest(ray.x, ray.y, ray.z);

                            WorldHelper.forBlocksInRay(playerClosest, pos, level, chunk, stateCache,
                                    (rayState, bpos) ->
                                    {   if (!bpos.equals(blockpos) && WorldHelper.isSpreadBlocked(level, rayState, bpos, direction, direction))
                                    {   blocks[0]++;
                                    }
                                    }, 3);

                            // Get the temperature of the block given the player's distance
                            double distance = MathHelper.getDistance(playerClosest, pos);

                            for (BlockTemp blockTemp : blockTemps)
                            {
                                double tempToAdd = blockTemp.getTemperature(level, entity, state, blockpos, distance);

                                // Store this block type's total effect on the player
                                // Dampen the effect with each block between the player and the block
                                double blockTempTotal = affectMap.getOrDefault(blockTemp, 0d) + tempToAdd / (blocks[0] + 1);
                                if (blockTempTotal < blockTemp.minEffect() || blockTempTotal > blockTemp.maxEffect()) continue;
                                affectMap.put(blockTemp, MathHelper.clamp(blockTempTotal, blockTemp.minEffect(), blockTemp.maxEffect()));
                                // Used to trigger advancements
                                if (shouldTickAdvancements)
                                {   triggers.add(new Triplet<>(blockpos, blockTemp, distance));
                                }
                            }
                        }
                    }
                    catch (Exception ignored) {}
                }
            }
        }
        // Trigger advancements at every BlockPos with a BlockEffect attached to it
        if (entity instanceof ServerPlayer player && shouldTickAdvancements)
        {

        }

        // Remove old chunks from the cache
        while (chunks.size() >= 16)
        {   chunks.remove(chunks.keySet().iterator().next());
        }

        // Add the effects of all the blocks together and return the result
        return temp ->
        {
            for (Map.Entry<BlockTemp, Double> effect : affectMap.entrySet())
            {
                BlockTemp be = effect.getKey();
                double min = be.minTemperature();
                double max = be.maxTemperature();
                if (!MathHelper.betweenInclusive(temp, min, max)) continue;
                temp = MathHelper.clamp(temp + effect.getValue(), min, max);
            }
            return temp;
        };
    }
}
