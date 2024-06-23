package net.shadowbeast.arcanemysteries.util.nbt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ListBuilder<T>
{
    ArrayList<T> elements = new ArrayList<>();

    @SafeVarargs
    private ListBuilder(T... elements)
    {   this.elements.addAll(Arrays.asList(elements));
    }

    private ListBuilder(T element)
    {   this.elements.add(element);
    }

    @SafeVarargs
    public static <E> ListBuilder<E> begin(E... elements)
    {   return new ListBuilder<>(elements);
    }

    public static <E> ListBuilder<E> begin(List<E> elements)
    {
        ListBuilder<E> builder = new ListBuilder<>();
        builder.elements.addAll(elements);
        return builder;
    }

    public static <E> ListBuilder<E> begin(E element)
    {   return new ListBuilder<>(element);
    }

    /**
     * Adds an element to the list if the condition is true.
     * The objects are not created if the condition is false.
     * @param condition The condition to check.
     * @param elements The elements to add.
     * @return The ListBuilder instance.
     */
    @SafeVarargs
    public final ListBuilder<T> addIf(boolean condition, Supplier<T>... elements)
    {   if (condition) this.elements.addAll(Arrays.stream(elements).map(Supplier::get).toList());
        return this;
    }

    public final ListBuilder<T> addAllIf(boolean condition, List<T> elements)
    {   if (condition) this.elements.addAll(elements);
        return this;
    }

    /**
     * Adds an element to the list if the condition is true.
     * The object is not created if the condition is false.
     * @param condition The condition to check.
     * @param supplier The element to add.
     * @return The ListBuilder instance.
     */
    public final ListBuilder<T> addIf(boolean condition, Supplier<T> supplier)
    {
        if (condition)
        {   T element = supplier.get();
            if (element != null)
            {   this.elements.add(element);
            }
        }
        return this;
    }

    @SafeVarargs
    public final ListBuilder<T> add(T... elements)
    {   this.elements.addAll(Arrays.asList(elements));
        return this;
    }

    public final ListBuilder<T> addAll(List<?> elements)
    {   this.elements.addAll((Collection<? extends T>) elements);
        return this;
    }

    public ListBuilder<T> add(T element)
    {   this.elements.add(element);
        return this;
    }

    public ArrayList<T> build()
    {   return elements;
    }
}
