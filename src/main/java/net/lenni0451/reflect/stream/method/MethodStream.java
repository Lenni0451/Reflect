package net.lenni0451.reflect.stream.method;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;
import net.lenni0451.reflect.stream.RStream;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A stream of all methods of a class and super classes (if wanted).<br>
 * The methods of super classes are after the methods of the class itself (descending order).
 */
public class MethodStream {

    private final RStream parent;
    private final List<MethodWrapper> methods;

    public MethodStream(final RStream parent, final boolean withSuper) {
        this.parent = parent;
        this.methods = new ArrayList<>();

        Class<?> clazz = parent.clazz();
        do {
            for (Method method : Methods.getDeclaredMethods(clazz)) this.methods.add(new MethodWrapper(this, method));

            if (!withSuper) break;
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }

    private MethodStream(final RStream parent, final List<MethodWrapper> methods) {
        this.parent = parent;
        this.methods = methods;
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
        return this.methods.size();
    }


    /**
     * Get a method by the given name.<br>
     * If there are multiple methods with the same name the first one will be returned.
     *
     * @param name The name of the method
     * @return The method wrapper
     */
    public Optional<MethodWrapper> opt(final String name) {
        for (MethodWrapper method : this.methods) {
            if (method.name().equals(name)) return Optional.of(method);
        }
        return Optional.empty();
    }

    /**
     * Get a method by the given parameter types.<br>
     * If there are multiple methods with the same parameter types the first one will be returned.
     *
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     */
    public Optional<MethodWrapper> opt(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (MethodWrapper method : this.methods) {
                if (method.parameterCount() == 0) return Optional.of(method);
            }
        } else {
            for (MethodWrapper method : this.methods) {
                if (Arrays.equals(method.parameterTypes(), parameterTypes)) return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    /**
     * Get a method by the given name and parameter types.
     *
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     */
    public Optional<MethodWrapper> opt(final String name, @Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (MethodWrapper method : this.methods) {
                if (method.name().equals(name) && method.parameterCount() == 0) return Optional.of(method);
            }
        } else {
            for (MethodWrapper method : this.methods) {
                if (method.name().equals(name) && Arrays.equals(method.parameterTypes(), parameterTypes)) return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    /**
     * Get a method by the given index.<br>
     * The index is the position of the method in the stream.
     *
     * @param index The index of the method
     * @return The method wrapper
     */
    public Optional<MethodWrapper> opt(final int index) {
        if (index < 0 || index >= this.methods.size()) return Optional.empty();
        return Optional.of(this.methods.get(index));
    }

    /**
     * Get a method by the given name.<br>
     * If there are multiple methods with the same name the first one will be returned.
     *
     * @param name The name of the method
     * @return The method wrapper
     * @throws MethodNotFoundException If the method doesn't exist
     */
    public MethodWrapper by(final String name) {
        return this.opt(name).orElseThrow(() -> new MethodNotFoundException(this.parent.clazz().getName(), name));
    }

    /**
     * Get a method by the given parameter types.<br>
     * If there are multiple methods with the same parameter types the first one will be returned.
     *
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     * @throws MethodNotFoundException If the method doesn't exist
     */
    public MethodWrapper by(@Nullable final Class<?>... parameterTypes) {
        return this.opt(parameterTypes).orElseThrow(() -> new MethodNotFoundException(this.parent.clazz().getName(), null, parameterTypes));
    }

    /**
     * Get a method by the given name and parameter types.
     *
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     * @throws MethodNotFoundException If the method doesn't exist
     */
    public MethodWrapper by(final String name, @Nullable final Class<?>... parameterTypes) {
        return this.opt(name, parameterTypes).orElseThrow(() -> new MethodNotFoundException(this.parent.clazz().getName(), name, parameterTypes));
    }

    /**
     * Get a method by the given index.<br>
     * The index is the position of the method in the stream.
     *
     * @param index The index of the method
     * @return The method wrapper
     * @throws MethodNotFoundException If the method doesn't exist
     */
    public MethodWrapper by(final int index) {
        try {
            return this.methods.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new MethodNotFoundException(this.parent.clazz().getName(), String.valueOf(index));
        }
    }


    /**
     * Filter the methods with the given filter.<br>
     * The current stream will be modified.
     *
     * @param filter The filter
     * @return This stream
     */
    public MethodStream filter(final Predicate<MethodWrapper> filter) {
        this.methods.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter the methods by the given name.<br>
     * The current stream will be modified.
     *
     * @param name The name of the method
     * @return This stream
     */
    public MethodStream filter(final String name) {
        return this.filter(method -> method.name().equals(name));
    }

    /**
     * Filter the methods by the given names.<br>
     * The current stream will be modified.
     *
     * @param names The names of the methods
     * @return This stream
     */
    public MethodStream filter(final Collection<String> names) {
        return this.filter(method -> names.contains(method.name()));
    }

    /**
     * Filter the methods by the given names.<br>
     * The current stream will be modified.
     *
     * @param names The names of the methods
     * @return This stream
     */
    public MethodStream filter(final String... names) {
        return this.filter(Arrays.asList(names));
    }

    /**
     * Filter the methods by the given parameter types.<br>
     * The current stream will be modified.
     *
     * @param parameterTypes The parameter types of the method
     * @return This stream
     */
    public MethodStream filter(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return this.filter(method -> method.parameterCount() == 0);
        } else {
            return this.filter(method -> Arrays.equals(method.parameterTypes(), parameterTypes));
        }
    }

    /**
     * Filter the methods by whether they are static.<br>
     * The current stream will be modified.
     *
     * @param isStatic Whether the methods should be static
     * @return This stream
     */
    public MethodStream filter(final boolean isStatic) {
        return this.filter(method -> method.modifier().isStatic() == isStatic);
    }

    /**
     * Filter the methods by whether they have the given annotation.<br>
     * The current stream will be modified.
     *
     * @param annotation The annotation
     * @return This stream
     */
    public MethodStream filterAnnotation(final Class<?> annotation) {
        return this.filter(method -> method.annotations().anyMatch(a -> a.annotationType().equals(annotation)));
    }


    /**
     * @return An iterator of method wrappers
     */
    public Iterator<MethodWrapper> iterator() {
        return this.methods.iterator();
    }

    /**
     * Map the methods to a new stream.<br>
     * This is the same as calling {@link #jstream()} and then {@link Stream#map(Function)}.
     *
     * @param mapFunction The map function
     * @param <T>         The type of the new stream
     * @return The new stream
     */
    public <T> Stream<T> map(final Function<MethodWrapper, T> mapFunction) {
        return this.jstream().map(mapFunction);
    }

    /**
     * @return A stream of method wrappers
     */
    public Stream<MethodWrapper> jstream() {
        return this.methods.stream();
    }

    /**
     * Loop through all methods in this stream.
     *
     * @param consumer The consumer
     * @return This stream
     */
    public MethodStream forEach(final Consumer<MethodWrapper> consumer) {
        this.methods.forEach(consumer);
        return this;
    }

    /**
     * @return A copy of this stream
     */
    public MethodStream copy() {
        return new MethodStream(this.parent, new ArrayList<>(this.methods));
    }

}
