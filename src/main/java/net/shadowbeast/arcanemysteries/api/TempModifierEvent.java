package net.shadowbeast.arcanemysteries.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.temprature.Temperature;

import java.util.function.Function;

/**
 * These events are fired when dealing with {@link TempModifier}s. <br>
 * They should not be side-specific. Do not limit them to run on any one side as it will cause desyncs.
 */
public class TempModifierEvent extends Event
{
    protected LivingEntity entity;
    protected Temperature.Trait trait;
    protected TempModifier modifier;

    protected TempModifierEvent(LivingEntity entity, Temperature.Trait trait, TempModifier modifier)
    {
        this.entity = entity;
        this.trait = trait;
        this.modifier = modifier;
    }

    public final LivingEntity getEntity()
    {   return entity;
    }

    public final Temperature.Trait getTrait()
    {   return trait;
    }

    public final TempModifier getModifier()
    {   return modifier;
    }

    /**
     * Fired when a {@link TempModifier} is about to be added to an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being applied to. <br>
     * {@link #trait} determines the modifier's {@link Temperature.Trait}. It will never be {@link Temperature.Trait#BODY} <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being added.<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Add extends TempModifierEvent
    {
        public Add(LivingEntity entity, Temperature.Trait trait, TempModifier modifier)
        {   super(entity, trait, modifier);
        }

        public void setTrait(Temperature.Trait newTrait)
        {   this.trait = newTrait;
        }

        public void setModifier(TempModifier modifier)
        {   this.modifier = modifier;
        }
    }

    /**
     * Fired when a {@link TempModifier} is about to be removed from an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being removed from. <br>
     * {@link #trait} is the modifier's {@link Temperature.Trait}. It will never be {@link Temperature.Trait#BODY}. <br>
     * {@link #count} is the number of TempModifiers of the specified class being removed. <br>
     * {@link #modifier} is the TempModifier being removed. <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being removed. <br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Remove extends TempModifierEvent
    {
        int count;

        public Remove(LivingEntity entity, Temperature.Trait trait, int count, TempModifier modifier)
        {
            super(entity, trait, modifier);
            this.count = count;
        }

        public void setCount(int count)
        {   this.count = count;
        }

        public int getCount()
        {   return count;
        }
    }


    /**
     * Fired when a TempModifier runs the {@code calculate()} method. <br>
     * {@code Pre} and {@code Post} are fired on the {@link MinecraftForge#EVENT_BUS} before/after the calculation respectively. <br>
     */
    public static class Calculate extends TempModifierEvent
    {
        protected double temperature;

        public Calculate(TempModifier modifier, LivingEntity entity, double temperature, Temperature.Trait trait)
        {
            super(entity, trait, modifier);
            this.temperature = temperature;
        }

        public double getTemperature()
        {   return temperature;
        }

        public void setTemperature(double temperature)
        {   this.temperature = temperature;
        }

        /**
         * Fired at the beginning of {@code calculate()}, before the {@code getValue()} method is called. <br>
         * <br>
         * {@link #entity} - The player the TempModifier is attached to. <br>
         * {@link #modifier} - The TempModifier running the method. <br>
         * {@link #temperature} - The Temperature being passed into the {@code getValue()} method. <br>
         * <br>
         * This event is {@link Cancelable}. <br>
         * Cancelling this event results in the modifier not being processed, remaining unchanged. <br>
         */
        @Cancelable
        public static class Override extends Calculate
        {
            public Override(TempModifier modifier, LivingEntity entity, double temperature, Temperature.Trait trait)
            {   super(modifier, entity, temperature, trait);
            }
        }

        /**
         * Fired by {@code calculate()} after the {@code getResult()} method is run, but before the value is returned <br>
         * <br>
         * {@link #entity} is the player the TempModifier is attached to. <br>
         * {@link #modifier} is the TempModifier running the method. <br>
         * {@link #temperature} is the Temperature after the {@code getValue())} method has been called. <br>
         * <br>
         * This event is NOT {@link Cancelable}. <br>
         */
        public static class Modify extends Calculate
        {
            private Function<Double, Double> newFunction;

            public Modify(TempModifier modifier, LivingEntity entity, double temp, Function<Double, Double> newFunction, Temperature.Trait trait)
            {   super(modifier, entity, temp, trait);
                this.newFunction = newFunction;
            }

            public Function<Double, Double> getFunction()
            {   return newFunction;
            }

            public void setFunction(Function<Double, Double> newFunction)
            {   this.newFunction = newFunction;
            }
        }

        public static class Process extends Calculate
        {
            public Process(TempModifier modifier, LivingEntity entity, double temperature, Temperature.Trait trait)
            {   super(modifier, entity, temperature, trait);
            }
        }
    }
}