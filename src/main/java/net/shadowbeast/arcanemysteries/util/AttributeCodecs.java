package net.shadowbeast.arcanemysteries.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeCodecs
{
    public static Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING.xmap(
            string -> AttributeModifier.Operation.valueOf(string.toUpperCase()),
            operation -> operation.name().toLowerCase()
    );

    public static Codec<AttributeModifier> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(AttributeModifier::getName),
                    Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::getAmount),
                    OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::getOperation)
            ).apply(instance, AttributeModifier::new)
    );

    public static Codec<Attribute> ATTRIBUTE_CODEC = ResourceLocation.CODEC.xmap(
            ForgeRegistries.ATTRIBUTES::getValue,
            ForgeRegistries.ATTRIBUTES::getKey
    );
}