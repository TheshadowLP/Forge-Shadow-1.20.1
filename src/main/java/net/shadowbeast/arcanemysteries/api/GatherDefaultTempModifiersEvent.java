package net.shadowbeast.arcanemysteries.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.api.util.Placement;
import net.shadowbeast.arcanemysteries.temprature.Temperature;

import java.util.ArrayList;
import java.util.List;

public class GatherDefaultTempModifiersEvent extends Event
{
    private final List<TempModifier> modifiers;
    private final LivingEntity entity;
    private final Temperature.Trait trait;

    public GatherDefaultTempModifiersEvent(LivingEntity entity, Temperature.Trait trait)
    {
        this.entity = entity;
        this.trait = trait;
        this.modifiers = new ArrayList<>(Temperature.getModifiers(entity, trait));
    }

    public List<TempModifier> getModifiers()
    {   return modifiers;
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public Temperature.Trait getTrait()
    {   return trait;
    }

    public void addModifier(TempModifier modifier)
    {   modifiers.add(modifier);
    }

    public void addModifiers(List<TempModifier> modifiers)
    {   this.modifiers.addAll(modifiers);
    }

    public void addModifier(TempModifier modifier, Placement.Duplicates duplicatePolicy, Placement params)
    {   Temperature.addModifier(modifiers, modifier, duplicatePolicy, 1, params);
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public void addModifier(TempModifier modifier, boolean allowDupes, Placement params)
    {   Temperature.addModifier(modifiers, modifier, allowDupes ? Placement.Duplicates.ALLOW : Placement.Duplicates.BY_CLASS, 1, params);
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public void addModifier(TempModifier modifier, boolean allowDupes, int maxDupes, Placement params)
    {   Temperature.addModifier(modifiers, modifier, allowDupes ? Placement.Duplicates.ALLOW : Placement.Duplicates.BY_CLASS, maxDupes, params);
    }
}