package net.shadowbeast.arcanemysteries.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowbeast.arcanemysteries.ArcaneMysteries;
import net.shadowbeast.arcanemysteries.block_entities.entities.AlloyFurnaceBlockEntity;
import net.shadowbeast.arcanemysteries.block_entities.entities.CrusherBlockEntity;
import net.shadowbeast.arcanemysteries.block_entities.entities.WinterFurnaceEntity;
import net.shadowbeast.arcanemysteries.blocks.ModBlocks;
import net.shadowbeast.arcanemysteries.entity.custom.ModHangingSignBlockEntity;
import net.shadowbeast.arcanemysteries.entity.custom.ModSignBlockEntity;
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ArcaneMysteries.MOD_ID);
    public static final RegistryObject<BlockEntityType<WinterFurnaceEntity>> WINTER_FURNACE_ENTITY =
            BLOCK_ENTITIES.register("winter_furnace_entity", () ->
                    BlockEntityType.Builder.of(WinterFurnaceEntity::new,
                            ModBlocks.WINTER_FURNACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alloy_furnace_block_entity", () ->
                    BlockEntityType.Builder.of(AlloyFurnaceBlockEntity::new,
                            ModBlocks.ALLOY_FURNACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("crusher_block_entity", () ->
                    BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> MOD_SIGN =
            BLOCK_ENTITIES.register("mod_sign", () ->
                    BlockEntityType.Builder.of(ModSignBlockEntity::new,
                            ModBlocks.FROZEN_SIGN.get(), ModBlocks.FROZEN_WALL_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN =
            BLOCK_ENTITIES.register("mod_hanging_sign", () ->
                    BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
                            ModBlocks.FROZEN_HANGING_SIGN.get(), ModBlocks.FROZEN_WALL_HANGING_SIGN.get()).build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
