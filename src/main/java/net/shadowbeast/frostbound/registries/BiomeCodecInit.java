package net.shadowbeast.frostbound.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowbeast.frostbound.Frostbound;
import net.shadowbeast.frostbound.core.data.AddSpawnsBiomeModifier;

public class BiomeCodecInit
{
    public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Frostbound.MOD_ID);

    public static RegistryObject<Codec<AddSpawnsBiomeModifier>> ADD_SPAWNS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
            RecordCodecBuilder.create(builder -> builder.group(
                    Codec.BOOL.fieldOf("use_configs").forGetter(AddSpawnsBiomeModifier::useConfigs)
            ).apply(builder, AddSpawnsBiomeModifier::new)));
}