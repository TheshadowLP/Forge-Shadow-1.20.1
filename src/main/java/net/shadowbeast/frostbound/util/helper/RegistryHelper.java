package net.shadowbeast.frostbound.util.helper;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import net.shadowbeast.frostbound.util.MathHelper;
import net.shadowbeast.frostbound.util.WorldHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistryHelper
{
    @Nullable
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registry)
    {   return MathHelper.getIfNotNull(getRegistryAccess(), access -> access.registryOrThrow(registry), null);
    }

    @Nullable
    public static RegistryAccess getRegistryAccess()
    {
        RegistryAccess access = null;

        MinecraftServer server = WorldHelper.getServer();

        if (server != null)
        {
            Level level = server.getLevel(Level.OVERWORLD);
            if (level != null)
            {   access = level.registryAccess();
            }
            else access = server.registryAccess();
        }

        if (access == null && FMLEnvironment.dist == Dist.CLIENT)
        {
            if (Minecraft.getInstance().level != null)
            {   access = Minecraft.getInstance().level.registryAccess();
            }
            else
            {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection != null)
                {   access = connection.registryAccess();
                }
            }
        }
        return access;
    }

    public static <T> List<T> mapForgeRegistryTagList(IForgeRegistry<T> registry, List<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey -> list.addAll(registry.tags().getTag(tagKey).stream().toList()));
            either.ifRight(object -> list.add(object));
        }
        return list;
    }

    public static <T> List<T> mapVanillaRegistryTagList(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, T>> eitherList, @Nullable RegistryAccess registryAccess)
    {
        Registry<T> reg = registryAccess != null ? registryAccess.registryOrThrow(registry) : getRegistry(registry);
        List<T> list = new ArrayList<>();
        if (reg == null) return list;

        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey ->
            {
                Optional<HolderSet.Named<T>> tag = reg.getTag(tagKey);
                tag.ifPresent(tag1 -> list.addAll(tag1.stream().map(Holder::value).toList()));
            });
            either.ifRight(list::add);
        }
        return list;
    }

    public static <T> Codec<Either<TagKey<T>, T>> createForgeTagCodec(IForgeRegistry<T> forgeRegistry, ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
                objectPath ->
                {
                    if (objectPath.startsWith("#"))
                    {   return Either.left(TagKey.create(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                    }
                    else
                    {   return Either.right(forgeRegistry.getValue(new ResourceLocation(objectPath)));
                    }
                },
                objectEither -> objectEither.map(
                        tagKey -> "#" + tagKey.location(),
                        object -> forgeRegistry.getKey(object).toString()
                ));
    }

    public static <T> Codec<Either<TagKey<T>, T>> createVanillaTagCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
                objectPath ->
                {
                    if (objectPath.startsWith("#"))
                    {   return Either.left(TagKey.create(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                    }
                    else
                    {   return Either.right(getVanillaRegistryValue(vanillaRegistry, new ResourceLocation(objectPath)).orElseThrow());
                    }
                },
                objectEither -> objectEither.map(
                        tagKey -> "#" + tagKey.location(),
                        object -> getVanillaRegistryKey(vanillaRegistry, object).orElseThrow().toString()
                ));
    }

    public static <T> Optional<T> getVanillaRegistryValue(ResourceKey< Registry<T>> registry, ResourceLocation id)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry)).map(reg -> reg.get(id));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    public static <T> Optional<ResourceLocation> getVanillaRegistryKey(ResourceKey<Registry<T>> registry, T value)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry)).map(reg -> reg.getKey(value));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    @Nullable
    public static Biome getBiome(ResourceLocation biomeId)
    {   return getVanillaRegistryValue(Registries.BIOME, biomeId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getBiomeId(Biome biome)
    {   return getVanillaRegistryKey(Registries.BIOME, biome).orElse(null);
    }

    @Nullable
    public static DimensionType getDimension(ResourceLocation dimensionId)
    {   return getVanillaRegistryValue(Registries.DIMENSION_TYPE, dimensionId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getDimensionId(DimensionType dimension)
    {   return getVanillaRegistryKey(Registries.DIMENSION_TYPE, dimension).orElse(null);
    }

    @Nullable
    public static StructureType<?> getStructure(ResourceLocation structureId)
    {   return getVanillaRegistryValue(Registries.STRUCTURE_TYPE, structureId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getStructureId(StructureType<?> structure)
    {   return getVanillaRegistryKey(Registries.STRUCTURE_TYPE, structure).orElse(null);
    }
}
