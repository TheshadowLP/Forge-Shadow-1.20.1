package net.shadowbeast.arcanemysteries.temprature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.shadowbeast.arcanemysteries.api.GatherDefaultTempModifiersEvent;
import net.shadowbeast.arcanemysteries.api.TempModifierEvent;
import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import net.shadowbeast.arcanemysteries.api.util.Placement;
import net.shadowbeast.arcanemysteries.networking.ModMessages;
import net.shadowbeast.arcanemysteries.networking.packet.TempModifiersSyncPacket;
import net.shadowbeast.arcanemysteries.networking.packet.TemperatureSyncPacket;
import net.shadowbeast.arcanemysteries.temprature.caps.TemperatureCap;
import net.shadowbeast.arcanemysteries.temprature.interfaces.ITemperature;
import net.shadowbeast.arcanemysteries.temprature.util.DummyPlayer;
import net.shadowbeast.arcanemysteries.temprature.util.EntityTempManager;
import net.shadowbeast.arcanemysteries.util.InterruptableStreamer;
import net.shadowbeast.arcanemysteries.util.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * General helper class for temperature-related actions. (Previously TempHelper)<br>
 * Allows for manipulating player temperature and TempModifiers.
 */
public class Temperature
{
    private Temperature() {}

    /**
     * Converts a double temperature to a different unit. If {@code from} and {@code to} are the same, returns {@code value}.<br>
     * @param value The temperature to convert.
     * @param from The unit to convert from.
     * @param to The unit to convert to.
     * @param absolute Used when dealing with absolute temperature.
     * @return The converted temperature.
     */
    public static double convert(double value, Units from, Units to, boolean absolute)
    {
        return switch (from)
        {
            case C -> switch (to)
            {
                case C -> value;
                case F -> value * 1.8 + (absolute ? 32d : 0d);
                case MC -> value / 25d;
            };
            case F -> switch (to)
            {
                case C -> (value - (absolute ? 32d : 0d)) / 1.8;
                case F -> value;
                case MC -> (value - (absolute ? 32d : 0d)) / 45d;
            };
            case MC -> switch (to)
            {
                case C -> value * 25d;
                case F -> value * 45d + (absolute ? 32d : 0d);
                case MC -> value;
            };
        };
    }

    /**
     * Returns the player's temperature of the specified type.
     */
    public static double get(LivingEntity entity, Trait trait)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.getTrait(trait)).orElse(0.0);
    }

    public static void set(LivingEntity entity, Trait trait, double value)
    {   EntityTempManager.getTemperatureCap(entity).orElse(new TemperatureCap()).setTrait(trait, value);
    }

    public static void add(LivingEntity entity, Trait trait, double value)
    {   EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.setTrait(trait, cap.getTrait(trait) + value));
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param entity the entity this modifier should use
     * @param modifiers the modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double currentTemp, LivingEntity entity, Trait trait, TempModifier... modifiers)
    {
        double temp2 = currentTemp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            double newTemp = entity.tickCount % modifier.getTickRate() == 0 || modifier.getTicksExisted() == 0 || entity.tickCount <= 1
                    ? modifier.update(temp2, entity, trait)
                    : modifier.process(temp2);
            if (!Double.isNaN(newTemp))
            {   temp2 = newTemp;
            }
        }
        return temp2;
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param entity the entity this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     */
    public static double apply(double temp, LivingEntity entity, Trait trait, Collection<TempModifier> modifiers)
    {   return apply(temp, entity, trait, modifiers.toArray(new TempModifier[0]));
    }

    static Map<ResourceLocation, DummyPlayer> DUMMIES = new HashMap<>();

    public static double getTemperatureAt(BlockPos pos, Level level)
    {
        ResourceLocation dimension = level.dimension().location();
        // There is one "dummy" entity per world, which TempModifiers are applied to
        DummyPlayer dummy = DUMMIES.get(dimension);
        // If the dummy for this dimension is invalid, make a new one
        if (dummy == null || dummy.level() != level)
        {
            DUMMIES.put(dimension, dummy = new DummyPlayer(level));
            // Use default player modifiers to determine the temperature
            GatherDefaultTempModifiersEvent event = new GatherDefaultTempModifiersEvent(dummy, Trait.WORLD);
            MinecraftForge.EVENT_BUS.post(event);
            addModifiers(dummy, event.getModifiers(), Trait.WORLD, Placement.Duplicates.BY_CLASS);
        }
        // Move the dummy to the position being tested
        dummy.setPos(MathHelper.getCenterPos(pos));
        return apply(0, dummy, Trait.WORLD, getModifiers(dummy, Trait.WORLD));
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param trait The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(LivingEntity entity, Trait trait, Class<? extends TempModifier> modClass)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.hasModifier(trait, modClass)).orElse(false);
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    public static <T extends TempModifier> Optional<T> getModifier(LivingEntity entity, Trait trait, Class<T> modClass)
    {   return getModifier(EntityTempManager.getTemperatureCap(entity).orElse(new TemperatureCap()), trait, modClass);
    }

    public static <T extends TempModifier> Optional<T> getModifier(ITemperature cap, Trait trait, Class<T> modClass)
    {   return (Optional<T>) cap.getModifiers(trait).stream().filter(modClass::isInstance).findFirst();
    }

    /**
     * @return The first modifier applied to the player that fits the predicate.
     */
    @Nullable
    public static TempModifier getModifier(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {
        for (TempModifier modifier : EntityTempManager.getTemperatureCap(entity).orElse(new TemperatureCap()).getModifiers(trait))
        {
            if (condition.test(modifier))
            {   return modifier;
            }
        }
        return null;
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public static void addOrReplaceModifier(LivingEntity entity, TempModifier modifier, Trait trait, boolean allowDupes)
    {   addOrReplaceModifier(entity, modifier, trait, allowDupes ? Placement.Duplicates.ALLOW : Placement.Duplicates.BY_CLASS);
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public static void replaceModifier(LivingEntity entity, TempModifier modifier, Trait trait)
    {   replaceModifier(entity, modifier, trait, Placement.Duplicates.BY_CLASS);
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, boolean allowDupes, int times, Placement placement)
    {   addModifier(entity, modifier, trait, allowDupes ? Placement.Duplicates.ALLOW : Placement.Duplicates.BY_CLASS, times, placement);
    }

    @Deprecated(since = "2.3-b03g", forRemoval = true)
    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, boolean allowDupes)
    {   addModifier(entity, modifier, trait, allowDupes ? Placement.Duplicates.ALLOW : Placement.Duplicates.BY_CLASS);
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * Otherwise, it will add the modifier.<br>
     * @param entity The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     */
    public static void addOrReplaceModifier(LivingEntity entity, TempModifier modifier, Trait trait, Placement.Duplicates duplicatePolicy)
    {   addModifier(entity, modifier, trait, duplicatePolicy, 1, Placement.of(Placement.Mode.REPLACE_OR_ADD, Placement.Order.FIRST, mod -> mod.equals(modifier)));
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * It will not add the modifier if an existing instance of the same TempModifier class is not found.<br>
     * @param entity The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     */
    public static void replaceModifier(LivingEntity entity, TempModifier modifier, Trait trait, Placement.Duplicates duplicates)
    {   addModifier(entity, modifier, trait, Placement.Duplicates.ALLOW, 1, Placement.of(Placement.Mode.REPLACE, Placement.Order.FIRST, mod -> Placement.Duplicates.check(duplicates, modifier, mod)));
    }

    /**
     * Adds the given modifier to the entity.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param duplicatePolicy allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, Placement.Duplicates duplicatePolicy)
    {   addModifier(entity, modifier, trait, duplicatePolicy, 1, Placement.AFTER_LAST);
    }

    /**
     * Adds the given modifier to the entity, with a custom placement.<br>
     */
    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, Placement.Duplicates duplicatePolicy, int times, Placement placement)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(entity, trait, modifier);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
            {
                if (addModifier(cap.getModifiers(trait), event.getModifier(), duplicatePolicy, times, placement))
                {   updateModifiers(entity, cap);
                }
            });
        }
    }

    public static boolean addModifier(List<TempModifier> modifiers, TempModifier modifier, Placement.Duplicates duplicatePolicy, int maxCount, Placement placement)
    {
        boolean changed = false;
        Predicate<TempModifier> predicate = placement.predicate();
        if (predicate == null) predicate = mod -> true;

        boolean isReplacing = placement.mode()  == Placement.Mode.REPLACE || placement.mode() == Placement.Mode.REPLACE_OR_ADD;
        boolean isForward = placement.order() == Placement.Order.FIRST;

        if (!isReplacing
                && modifiers.stream().anyMatch(mod -> Placement.Duplicates.check(duplicatePolicy, modifier, mod)))
        {   return false;
        }

        // The number of TempModifiers that match the predicate
        int hits = 0;
        // Get the start of the iterator & which direction it's going
        int start = isForward ? 0 : (modifiers.size() - 1);
        // Iterate through the list (backwards if "forward" is false)
        for (int i = start; isForward ? i < modifiers.size() : i >= 0; i += isForward ? 1 : -1)
        {
            TempModifier mod = modifiers.get(i);
            // If the predicate is true, inject the modifier at this position (or after it if "after" is true)
            if (predicate.test(mod))
            {
                if (isReplacing)
                {   modifiers.set(i, modifier);
                }
                else
                {   modifiers.add(i + (placement.mode()  == Placement.Mode.AFTER ? 1 : 0), modifier);
                }
                hits++;
                changed = true;
                // If duplicates are not allowed, break the loop
                if (duplicatePolicy != Placement.Duplicates.ALLOW || hits >= maxCount)
                {   return true;
                }
            }
        }
        // Add the modifier if the insertion check fails
        if (placement.mode() != Placement.Mode.REPLACE)
        {
            modifiers.add(modifier);
            changed = true;
        }
        return changed;
    }

    public static void addModifiers(LivingEntity entity, List<TempModifier> modifiers, Trait trait, Placement.Duplicates duplicatePolicy)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            for (TempModifier modifier : modifiers)
            {   addModifier(entity, modifier, trait, duplicatePolicy);
            }
            updateModifiers(entity, cap);
        });
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param entity The entity being sampled
     * @param trait Determines which TempModifier list to pull from
     * @param maxCount The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(LivingEntity entity, Trait trait, int maxCount, Placement.Order order, Predicate<TempModifier> condition)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            List<TempModifier> modifiers = cap.getModifiers(trait);
            boolean forwardOrder = order == Placement.Order.FIRST;
            int removed = 0;

            for (int i = forwardOrder ? 0 : modifiers.size() - 1; i >= 0 && i < modifiers.size(); i += forwardOrder ? 1 : -1)
            {
                if (removed < maxCount)
                {
                    TempModifier modifier = modifiers.get(i);
                    if (condition.test(modifier))
                    {
                        TempModifierEvent.Remove event = new TempModifierEvent.Remove(entity, trait, maxCount, modifier);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (!event.isCanceled())
                        {
                            removed++;
                            modifiers.remove(i);
                            i += forwardOrder ? -1 : 1;
                        }
                    }

                }
                else break;
            }

            // Update modifiers if anything actually changed
            if (removed > 0)
            {   updateModifiers(entity, cap);
            }
        });
    }

    public static void removeModifiers(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {   removeModifiers(entity, trait, Integer.MAX_VALUE, Placement.Order.FIRST, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param entity is the entity being sampled
     * @param trait determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(LivingEntity entity, Trait trait)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.getModifiers(trait)).orElse(List.of());
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param trait determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(LivingEntity entity, Trait trait, Consumer<TempModifier> action)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            if (cap.getModifiers(trait) != null)
            {   cap.getModifiers(trait).forEach(action);
            }
        });
    }

    public static void forEachModifier(LivingEntity entity, Trait trait, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            if (cap.getModifiers(trait) != null)
            {   MathHelper.breakableForEach(cap.getModifiers(trait), action);
            }
        });
    }

    public static void updateTemperature(LivingEntity entity, ITemperature cap, boolean instant)
    {
        if (!entity.level().isClientSide)
        {
            ModMessages.INSTANCE.send(entity instanceof ServerPlayer player
                            ? PacketDistributor.PLAYER.with(() -> player)
                            : PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new TemperatureSyncPacket(entity, cap.serializeTraits(), instant));
        }
    }

    public static void updateModifiers(LivingEntity entity, ITemperature cap)
    {
        if (!entity.level().isClientSide)
        {
            ModMessages.INSTANCE.send(entity instanceof ServerPlayer player
                            ? PacketDistributor.PLAYER.with(() -> player)
                            : PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                    new TempModifiersSyncPacket(entity, cap.serializeModifiers()));
        }
    }

    public static Map<Trait, Double> getTemperatures(LivingEntity entity)
    {   return EntityTempManager.getTemperatureCap(entity).map(ITemperature::getTraits).orElse(new EnumMap<>(Trait.class));
    }

    /**
     * Defines all temperature stats in Cold Sweat. <br>
     * These are used to get temperature stored on the player and/or to apply modifiers to it. <br>
     * <br>
     * {@link #WORLD}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * <br>
     * {@link #CORE}: The core temperature of the player (This is what "body" temperature typically refers to). <br>
     * {@link #BASE}: A static offset applied to the player's core temperature. <br>
     * {@link #BODY}: The sum of the player's core and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     * {@link #FREEZING_POINT}: An offset to the max temperature threshold, after which a player's body temperature starts rising. <br>
     * {@link #BURNING_POINT}: An offset to the min temperature threshold, after which a player's body temperature starts falling. <br>
     * {@link #COLD_RESISTANCE}: Resistance to cold temperature-related damage. <br>
     * {@link #HEAT_RESISTANCE}: Resistance to heat temperature-related damage. <br>
     * {@link #COLD_DAMPENING}: Changes the rate of body temperature increase. <br>
     * {@link #HEAT_DAMPENING}: Changes the rate of body temperature decrease. <br>
     */
    public enum Trait implements StringRepresentable
    {
        WORLD("world"),
        CORE("core"),
        BASE("base"),
        BODY("body"),
        RATE("rate"),

        FREEZING_POINT("freezing_point"),
        BURNING_POINT("burning_point"),
        COLD_RESISTANCE("cold_resistance"),
        HEAT_RESISTANCE("heat_resistance"),
        COLD_DAMPENING("cold_dampening"),
        HEAT_DAMPENING("heat_dampening");

        public static final Codec<Trait> CODEC = StringRepresentable.fromEnum(Trait::values);

        private final String id;

        Trait(String id)
        {   this.id = id;
        }

        public static Trait fromID(String id)
        {
            for (Trait trait : values())
            {
                if (trait.getSerializedName().equals(id))
                    return trait;
            }
            return null;
        }

        @Override
        public String getSerializedName()
        {   return id;
        }
    }

    /**
     * Units of measurement used by Cold Sweat.<br>
     * Most calculations are done in MC units, then converted to C or F when they are displayed.<br>
     */
    public enum Units implements StringRepresentable
    {
        F("°F", "f"),
        C("°C", "c"),
        MC("MC", "mc");

        public static final Codec<Units> CODEC = StringRepresentable.fromEnum(Units::values);

        private final String name;
        private final String id;

        Units(String name, String id)
        {   this.name = name;
            this.id = id;
        }

        public static Units fromID(String id)
        {
            for (Units unit : values())
            {
                if (unit.getSerializedName().equals(id))
                    return unit;
            }
            return null;
        }

        public String getFormattedName()
        {   return name;
        }

        @Override
        public String getSerializedName()
        {   return id;
        }
    }

}