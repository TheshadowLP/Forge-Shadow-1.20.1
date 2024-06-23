package net.shadowbeast.arcanemysteries.temprature.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.config.ConfigSettings;
import net.shadowbeast.arcanemysteries.temprature.Temperature;
import net.shadowbeast.arcanemysteries.util.MathHelper;
import net.shadowbeast.arcanemysteries.util.WorldHelper;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {
        this(0.01);
    }

    public WaterTempModifier(double strength)
    {   this.getNBT().putDouble("Strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double worldTemp = Temperature.get(entity, Temperature.Trait.WORLD);
        double maxTemp = ConfigSettings.MAX_TEMP.get();
        double minTemp = ConfigSettings.MIN_TEMP.get();

        double strength = this.getNBT().getDouble("Strength");
        double returnRate = Math.min(-0.0012, -0.0012 - (worldTemp / 640));
        double addAmount = WorldHelper.isInWater(entity) ? 0.05 : WorldHelper.isRainingAt(entity.level(), entity.blockPosition()) ? 0.0125 : returnRate;
        double maxStrength = MathHelper.clamp(Math.abs(MathHelper.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);

        double newStrength = MathHelper.clamp(strength + addAmount, 0d, maxStrength);
        this.getNBT().putDouble("Strength", newStrength);

        // If the strength is 0, this TempModifier expires
        if (strength <= 0.0)
        {   this.expires(this.getTicksExisted() - 1);
        }

        return temp ->
        {
            if (!entity.isInWater())
            {
                if (Math.random() < strength * 2)
                {   double randX = entity.getBbWidth() * (Math.random() - 0.5);
                    double randY = entity.getBbHeight() * Math.random();
                    double randZ = entity.getBbWidth() * (Math.random() - 0.5);
                    entity.level().addParticle(ParticleTypes.FALLING_WATER, entity.getX() + randX, entity.getY() + randY, entity.getZ() + randZ, 0, 0, 0);
                }
            }
            return temp - newStrength;
        };
    }
}