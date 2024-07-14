package net.shadowbeast.arcanemysteries.api.util;


import net.shadowbeast.arcanemysteries.api.temperature.TempModifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public record Placement(Mode mode, Order order, Predicate<TempModifier> predicate)
{
    public static final Placement AFTER_LAST = Placement.of(Mode.AFTER, Order.LAST, mod -> true);
    public static final Placement BEFORE_FIRST = Placement.of(Mode.BEFORE, Order.FIRST, mod -> true);

    @Contract("_, _, _ -> new")
    public static @NotNull Placement of(Mode mode, Order order, Predicate<TempModifier> predicate)
    {
        return new Placement(mode, order, predicate);
    }

    public enum Mode
    {
        // Inserts the new modifier before the targeted modifier's position
        BEFORE,
        // Inserts the new modifier after the targeted modifier's position
        AFTER,
        // Replace the desired instance of the modifier (fails if no modifiers pass the predicate)
        REPLACE,
        // Replace the desired instance of the modifier if it exists, otherwise add it to the end
        REPLACE_OR_ADD
    }

    public enum Order
    {
        // Targets the first modifier that passes the predicate
        FIRST,
        // Targets the last modifier that passes the predicate
        LAST
    }

    public enum Duplicates
    {
        // Allow duplicate TempModifiers
        ALLOW,
        // Disallow duplicate TempModifiers (ignores NBT)
        BY_CLASS,
        // Disallow duplicate TempModifiers only if they have the same NBT
        EXACT;

        public static boolean check(@NotNull Duplicates policy, TempModifier modA, TempModifier modB)
        {
            return switch (policy)
            {
                case ALLOW    -> false;
                case BY_CLASS -> modA.getClass().equals(modB.getClass());
                case EXACT    -> modA.equals(modB);
            };
        }
    }
}