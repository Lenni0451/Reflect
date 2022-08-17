package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.utils.Sneaky;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An easy-to-use stream to filter out the constructors you want to access
 */
public class ConstructorStream {

    private final RStream parent;
    private final List<ConstructorWrapper> constructors;

    public ConstructorStream(final RStream parent) {
        this.parent = parent;
        this.constructors = new ArrayList<>();

        for (Constructor<?> constructor : Constructors.getDeclaredConstructors(parent.clazz())) this.constructors.add(new ConstructorWrapper(this, constructor));
    }

    /**
     * Get the parent {@link RStream} instance
     */
    public RStream parent() {
        return this.parent;
    }

    /**
     * Get the amount of constructors in this stream
     */
    public int size() {
        return this.constructors.size();
    }


    /**
     * Get the {@link ConstructorWrapper} instance of the constructor with the given parameter types
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The {@link ConstructorWrapper} instance
     * @throws NoSuchMethodException If the constructor doesn't exist
     */
    public ConstructorWrapper by(final Class<?>... parameterTypes) {
        for (ConstructorWrapper constructor : this.constructors) {
            if (Arrays.equals(constructor.parameterTypes(), parameterTypes)) return constructor;
        }
        Sneaky.sthrow(new NoSuchMethodException());
        return null;
    }

    /**
     * Get the {@link ConstructorWrapper} instance of the constructor with the given index
     *
     * @param index The index of the constructor
     * @return The {@link ConstructorWrapper} instance
     * @throws NoSuchMethodException If the constructor doesn't exist
     */
    public ConstructorWrapper by(final int index) {
        try {
            return this.constructors.get(index);
        } catch (IndexOutOfBoundsException e) {
            Sneaky.sthrow(new NoSuchMethodException());
        }
        return null;
    }


    /**
     * Filter the constructors by the given predicate
     *
     * @param filter The predicate
     * @return The filtered {@link ConstructorStream}
     */
    public ConstructorStream filter(final Predicate<ConstructorWrapper> filter) {
        this.constructors.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter the constructors by the given parameter types
     *
     * @param parameterTypes The parameter types
     * @return The filtered {@link ConstructorStream}
     */
    public ConstructorStream filter(final Class<?>... parameterTypes) {
        return this.filter(constructor -> Arrays.equals(constructor.parameterTypes(), parameterTypes));
    }

    /**
     * Filter out all static/non-static constructors
     *
     * @param isStatic Whether the constructor should be static or not
     * @return The filtered {@link ConstructorStream}
     */
    public ConstructorStream filter(final boolean isStatic) {
        return this.filter(constructor -> constructor.modifier().isStatic() == isStatic);
    }


    /**
     * Get an iterator of the {@link ConstructorWrapper} instances
     */
    public Iterator<ConstructorWrapper> iterator() {
        return this.constructors.iterator();
    }

    /**
     * Get the java stream of the {@link ConstructorWrapper} instances
     */
    public Stream<ConstructorWrapper> jstream() {
        return this.constructors.stream();
    }

    /**
     * Loop through all {@link ConstructorWrapper} instances
     *
     * @param consumer The consumer
     * @return The {@link ConstructorStream}
     */
    public ConstructorStream forEach(final Consumer<ConstructorWrapper> consumer) {
        this.constructors.forEach(consumer);
        return this;
    }

}
