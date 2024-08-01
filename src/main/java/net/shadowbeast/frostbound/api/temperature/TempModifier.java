package net.shadowbeast.frostbound.api.temperature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.shadowbeast.frostbound.api.TempModifierEvent;
import net.shadowbeast.frostbound.registries.TempModifierRegistry;
import net.shadowbeast.frostbound.temprature.Temperature;

import java.util.function.Function;
public abstract class TempModifier
{
    private CompoundTag nbt = new CompoundTag();
    private int expireTicks = -1;
    private int ticksExisted = 0;
    private int tickRate = 1;
    private double lastInput = 0;
    private double lastOutput = 0;
    private Function<Double, Double> function = temp -> temp;

    /**
     * Default constructor (REQUIRED for proper registration).<br>
     */
    public TempModifier() {}

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.<br>
     * This is basically a simple in-out system. It is given a temperature, and returns a new temperature based on the PlayerEntity.<br>
     * <br>
     * @param entity the entity that is being affected by the modifier.<br>
     * @return the new temperature.<br>
     */
    protected abstract Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait);

    /**
     * Posts this TempModifier's {@link #calculate(LivingEntity, Temperature.Trait)} to the Forge event bus.<br>
     * Returns the stored value if this TempModifier has a tickRate set, and it is not the right tick.<br>
     * <br>
     * @param temp the Temperature being fed into the {@link #calculate(LivingEntity, Temperature.Trait)} method.
     * @param entity the entity that is being affected by the modifier.
     */
    public final double update(double temp, LivingEntity entity, Temperature.Trait trait)
    {
        TempModifierEvent.Calculate.Override override = new TempModifierEvent.Calculate.Override(this, entity, temp, trait);
        MinecraftForge.EVENT_BUS.post(override);
        if (override.isCanceled())
        {
            this.function = t -> override.getTemperature();
            return override.getTemperature();
        }

        TempModifierEvent.Calculate.Modify modify = new TempModifierEvent.Calculate.Modify(this, entity, override.getTemperature(), this.calculate(entity, trait), trait);
        MinecraftForge.EVENT_BUS.post(modify);

        this.function = modify.getFunction();

        return this.process(modify.getTemperature());
    }

    /**
     * @param temp the Temperature to calculate with
     * @return The result of this TempModifier's unique stored function. Stores the input and output.
     */
    public final double process(double temp)
    {
        lastInput = temp;
        return lastOutput = function.apply(temp);
    }

    /**
     * Sets the number of ticks this TempModifier will exist before it is automatically removed.<br>
     * @param ticks the number of ticks this modifier will last.
     * @return this TempModifier instance (allows for in-line building).
     */
    public final TempModifier expires(int ticks)
    {
        expireTicks = ticks;
        return this;
    }
    public final int getExpireTime()
    {   return expireTicks;
    }
    public final int getTicksExisted()
    {   return ticksExisted;
    }
    public final int setTicksExisted(int ticks)
    {   return ticksExisted = ticks;
    }

    /**
     * TempModifiers can be configured to run {@link TempModifier#calculate(LivingEntity, Temperature.Trait)} at a specified interval.<br>
     * This is useful if the TempModifier is expensive to calculate, and you want to avoid it being called each tick.<br>
     * <br>
     * Every X ticks, the TempModifier's {@code getResult()} function will be called, then stored internally.<br>
     * Every other time {@code calculate()} is called, the stored value will be returned until X ticks have passed.<br>
     * (new TempModifiers ALWAYS run {@code getResult()} when they are called for the first time).<br>
     * <br>
     * @param ticks the number of ticks between each call to {@code getResult()}.
     * @return this TempModifier instance (allows for in-line building).
     */
    public final TempModifier tickRate(int ticks)
    {   tickRate = Math.max(1, ticks);
        return this;
    }

    public final int getTickRate()
    {   return tickRate;
    }

    /**
     * @return The Temperature this TempModifier was last given
     */
    public final double getLastInput()
    {   return lastInput;
    }

    /**
     * @return The Temperature this TempModifier's function last returned
     */
    public final double getLastOutput()
    {   return lastOutput;
    }

    public final CompoundTag getNBT()
    {
        return nbt;
    }

    public void setNBT(CompoundTag data)
    {
        this.nbt = data;
    }

    @Override
    public String toString()
    {   return TempModifierRegistry.getKey(this).toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TempModifier mod
                && this.getClass().equals(mod.getClass())
                && mod.getNBT().equals(this.getNBT());
    }
}