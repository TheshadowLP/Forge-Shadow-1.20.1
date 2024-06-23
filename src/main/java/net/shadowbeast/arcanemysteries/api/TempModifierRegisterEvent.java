package net.shadowbeast.arcanemysteries.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.registries.TempModifierRegistry;
import net.shadowbeast.arcanemysteries.util.RegistryFailureException;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

/**
 * Builds the {@link TempModifierRegistry}. <br>
 * The event is fired during {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}. <br>
 * <br>
 * This event is NOT {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
public class TempModifierRegisterEvent extends Event
{
    /**
     * Adds a new {@link TempModifier} to the registry.
     *
     * @param modifier the {@link TempModifier} to add.
     */
    public void register(ResourceLocation id, Supplier<TempModifier> modifier)
    {   TempModifierRegistry.register(id, modifier);
    }

    /**
     * A way of indirectly registering TempModifiers by class name.<br>
     * Useful for adding compat for other mods, where loading the TempModifier's class directly would cause an error.<br>
     * The class must have a no-arg constructor for this to work.
     */
    public void registerByClassName(ResourceLocation id, String className)
    {
        try
        {
            Constructor<?> clazz = Class.forName(className).getConstructor();
            this.register(id, () ->
            {
                try
                {   return (TempModifier) clazz.newInstance();
                }
                catch (Exception e)
                {   throw new RegistryFailureException(id, "TempModifier", "Failed to instantiate class " + className, e);
                }
            });
        }
        catch (Exception e)
        {   throw new RegistryFailureException(id, "TempModifier", e.getMessage(), e);
        }
    }
}
