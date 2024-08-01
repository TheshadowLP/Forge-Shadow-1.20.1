package net.shadowbeast.frostbound.api.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.shadowbeast.frostbound.registries.ModAttributes;

import static net.shadowbeast.frostbound.temprature.util.EntityTempManager.TEMPERATURE_ENABLED_ENTITIES;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddTempAttributes
{
    @SubscribeEvent
    public static void onEntitiesCreated(EntityAttributeModificationEvent event)
    {
        for (EntityType<? extends LivingEntity> type : event.getTypes())
        {
            if (type != EntityType.PLAYER)
            {   EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(type);
                ModLoader.get().postEvent(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) continue;
            }
            TEMPERATURE_ENABLED_ENTITIES.add(type);

            event.add(type, ModAttributes.COLD_DAMPENING, Double.NaN);
            event.add(type, ModAttributes.HEAT_DAMPENING, Double.NaN);
            event.add(type, ModAttributes.COLD_RESISTANCE, Double.NaN);
            event.add(type, ModAttributes.HEAT_RESISTANCE, Double.NaN);
            event.add(type, ModAttributes.BURNING_POINT, Double.NaN);
            event.add(type, ModAttributes.FREEZING_POINT, Double.NaN);
            event.add(type, ModAttributes.BASE_BODY_TEMPERATURE, Double.NaN);
            event.add(type, ModAttributes.WORLD_TEMPERATURE, Double.NaN);
        }
    }

    @SubscribeEvent
    public static void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {

    }
}
