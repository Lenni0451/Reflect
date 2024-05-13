package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.exceptions.ConstructorNotFoundException;
import net.lenni0451.reflect.stream.RStream;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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

    private ConstructorStream(final RStream parent, final List<ConstructorWrapper> constructors) {
        this.parent = parent;
        this.constructors = constructors;
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
     */
    public Optional<ConstructorWrapper> opt(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (ConstructorWrapper constructor : this.constructors) {
                if (constructor.parameterTypes().length == 0) return Optional.of(constructor);
            }
        } else {
            for (ConstructorWrapper constructor : this.constructors) {
                if (Arrays.equals(constructor.parameterTypes(), parameterTypes)) return Optional.of(constructor);
            }
        }
        return Optional.empty();
    }

    /**
     * Get a constructor by the given index.<br>
     * The index is the position of the constructor in the stream.
     *
     * @param index The index of the constructor
     * @return The constructor wrapper
     */
    public Optional<ConstructorWrapper> opt(final int index) {
        if (index < 0 || index > this.constructors.size()) return Optional.empty();
        return Optional.of(this.constructors.get(index));
    }

    /**
     * Get a constructor by the given parameter types.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor wrapper
     * @throws ConstructorNotFoundException If the constructor doesn't exist
     */
    public ConstructorWrapper by(@Nullable final Class<?>... parameterTypes) {
        return this.opt(parameterTypes).orElseThrow(() -> new ConstructorNotFoundException(this.parent.clazz().getName(), parameterTypes));
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
        return this.opt(index).orElseThrow(() -> new ConstructorNotFoundException(this.parent.clazz().getName(), String.valueOf(index)));
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
     * Filter the constructors by whether they have the given annotation.<br>
     * The current stream will be modified.
     *
     * @param annotation The annotation
     * @return This stream
     */
    public ConstructorStream filterAnnotation(final Class<?> annotation) {
        return this.filter(constructor -> constructor.annotations().anyMatch(a -> a.annotationType().equals(annotation)));
    }


    /**
     * @return An iterator of method wrappers
     */
    public Iterator<ConstructorWrapper> iterator() {
        return this.constructors.iterator();
    }

    /**
     * Map the constructors to a new stream.<br>
     * This is the same as calling {@link #jstream()} and then {@link Stream#map(Function)}.
     *
     * @param mapFunction The map function
     * @param <T>         The type of the new stream
     * @return The new stream
     */
    public <T> Stream<T> map(final Function<ConstructorWrapper, T> mapFunction) {
        return this.jstream().map(mapFunction);
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

    /**
     * @return A copy of this stream
     */
    public ConstructorStream copy() {
        return new ConstructorStream(this.parent, new ArrayList<>(this.constructors));
    }

}
