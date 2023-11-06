package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.exceptions.ConstructorNotFoundException;
import net.lenni0451.reflect.stream.RStream;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A stream of all constructors of a class.
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
     * @return The parent stream
     */
    public RStream parent() {
        return this.parent;
    }

    /**
     * @return The amount of methods in this stream
     */
    public int size() {
        return this.constructors.size();
    }


    /**
     * Get a constructor by the given parameter types.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor wrapper
     * @throws ConstructorNotFoundException If the constructor doesn't exist
     */
    public ConstructorWrapper by(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (ConstructorWrapper constructor : this.constructors) {
                if (constructor.parameterTypes().length == 0) return constructor;
            }
        } else {
            for (ConstructorWrapper constructor : this.constructors) {
                if (Arrays.equals(constructor.parameterTypes(), parameterTypes)) return constructor;
            }
        }
        throw new ConstructorNotFoundException(this.parent.clazz().getName(), parameterTypes);
    }

    /**
     * Get a constructor by the given index.<br>
     * The index is the position of the constructor in the stream.
     *
     * @param index The index of the constructor
     * @return The constructor wrapper
     * @throws ConstructorNotFoundException If the constructor doesn't exist
     */
    public ConstructorWrapper by(final int index) {
        try {
            return this.constructors.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new ConstructorNotFoundException(this.parent.clazz().getName(), String.valueOf(index));
        }
    }


    /**
     * Filter the methods with the given filter.<br>
     * The current stream will be modified.
     *
     * @param filter The filter
     * @return This stream
     */
    public ConstructorStream filter(final Predicate<ConstructorWrapper> filter) {
        this.constructors.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter the methods by the given parameter types.<br>
     * The current stream will be modified.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return This stream
     */
    public ConstructorStream filter(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return this.filter(constructor -> constructor.parameterCount() == 0);
        } else {
            return this.filter(constructor -> Arrays.equals(constructor.parameterTypes(), parameterTypes));
        }
    }

    /**
     * Filter the constructors by whether they are static.<br>
     * The current stream will be modified.
     *
     * @param isStatic Whether the constructor should be static
     * @return This stream
     */
    public ConstructorStream filter(final boolean isStatic) {
        return this.filter(constructor -> constructor.modifier().isStatic() == isStatic);
    }


    /**
     * @return An iterator of method wrappers
     */
    public Iterator<ConstructorWrapper> iterator() {
        return this.constructors.iterator();
    }

    /**
     * @return A stream of method wrappers
     */
    public Stream<ConstructorWrapper> jstream() {
        return this.constructors.stream();
    }

    /**
     * Loop through all constructors in this stream.
     *
     * @param consumer The consumer
     * @return This stream
     */
    public ConstructorStream forEach(final Consumer<ConstructorWrapper> consumer) {
        this.constructors.forEach(consumer);
        return this;
    }

}
