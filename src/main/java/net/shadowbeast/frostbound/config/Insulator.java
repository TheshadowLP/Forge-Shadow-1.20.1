package net.shadowbeast.frostbound.config;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.shadowbeast.frostbound.api.insulation.Insulation;
import net.shadowbeast.frostbound.codec.requierment.EntityRequirement;
import net.shadowbeast.frostbound.codec.requierment.ItemRequirement;
import net.shadowbeast.frostbound.util.AttributeModifierMap;
import net.shadowbeast.frostbound.util.nbt.NbtSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record Insulator(Insulation insulation, Insulation.Slot slot, ItemRequirement data,
                        EntityRequirement predicate, AttributeModifierMap attributes,
                        Map<ResourceLocation, Double> immuneTempModifiers) implements NbtSerializable
{
    public boolean test(Entity entity, ItemStack stack)
    {   return predicate.test(entity) && data.test(stack, true);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("insulation", insulation.serialize());
        tag.put("slot", Insulation.Slot.CODEC.encodeStart(NbtOps.INSTANCE, slot).result().get());
        tag.put("data", data.serialize());
        tag.put("predicate", predicate.serialize());
        tag.put("attributes", attributes.serialize());
        CompoundTag immuneTempModifiersTag = new CompoundTag();
        immuneTempModifiers.forEach((key, value) -> immuneTempModifiersTag.putDouble(key.toString(), value));
        tag.put("immune_temp_modifiers", immuneTempModifiersTag);

        return tag;
    }

    public static Insulator deserialize(CompoundTag tag)
    {
        Insulation insulation = Insulation.deserialize(tag.getCompound("insulation"));
        Insulation.Slot slot = Insulation.Slot.CODEC.parse(NbtOps.INSTANCE, tag.get("slot")).result().get();
        ItemRequirement data = ItemRequirement.deserialize(tag.getCompound("data"));
        EntityRequirement predicate = EntityRequirement.deserialize(tag.getCompound("predicate"));
        AttributeModifierMap attributes = AttributeModifierMap.deserialize(tag.getCompound("attributes"));
        CompoundTag immuneTempModifiersTag = tag.getCompound("immune_temp_modifiers");
        Map<ResourceLocation, Double> immuneTempModifiers = new HashMap<>();
        immuneTempModifiersTag.getAllKeys().forEach(key -> immuneTempModifiers.put(new ResourceLocation(key), immuneTempModifiersTag.getDouble(key)));

        return new Insulator(insulation, slot, data, predicate, attributes, immuneTempModifiers);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Insulator insulator = (Insulator) obj;

        return insulation.equals(insulator.insulation)
                && data.equals(insulator.data)
                && predicate.equals(insulator.predicate)
                && attributes.equals(insulator.attributes);
    }
}
