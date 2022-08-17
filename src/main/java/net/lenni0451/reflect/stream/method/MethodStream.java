package net.lenni0451.reflect.stream.method;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.utils.Sneaky;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An easy-to-use stream to filter out the methods you want to access
 */
public class MethodStream {

    private final RStream parent;
    private final List<MethodWrapper> methods;

    public MethodStream(final RStream parent) {
        this.parent = parent;
        this.methods = new ArrayList<>();

        for (Method method : Methods.getDeclaredMethods(parent.clazz())) this.methods.add(new MethodWrapper(this, method));
    }

    /**
     * Get the parent {@link RStream} instance
     */
    public RStream parent() {
        return this.parent;
    }

    /**
     * Get the amount of methods in this stream
     */
    public int size() {
        return this.methods.size();
    }


    /**
     * Get the {@link MethodWrapper} instance of the method with the given name
     *
     * @param name The name of the method
     * @return The {@link MethodWrapper} instance
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final String name) {
        for (MethodWrapper method : this.methods) {
            if (method.name().equals(name)) return method;
        }
        Sneaky.sthrow(new NoSuchMethodException());
        return null;
    }

    /**
     * Get the {@link MethodWrapper} instance of the method with the given parameter types
     *
     * @param parameterTypes The parameter types of the method
     * @return The {@link MethodWrapper} instance
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final Class<?>... parameterTypes) {
        for (MethodWrapper method : this.methods) {
            if (Arrays.equals(method.parameterTypes(), parameterTypes)) return method;
        }
        Sneaky.sthrow(new NoSuchMethodException());
        return null;
    }

    /**
     * Get the {@link MethodWrapper} instance of the method with the given name and parameter types
     *
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The {@link MethodWrapper} instance
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final String name, final Class<?>... parameterTypes) {
        for (MethodWrapper method : this.methods) {
            if (method.name().equals(name) && Arrays.equals(method.parameterTypes(), parameterTypes)) return method;
        }
        Sneaky.sthrow(new NoSuchMethodException());
        return null;
    }

    /**
     * Get the {@link MethodWrapper} instance of the method with the given index
     *
     * @param index The index of the method
     * @return The {@link MethodWrapper} instance
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final int index) {
        try {
            return this.methods.get(index);
        } catch (IndexOutOfBoundsException e) {
            Sneaky.sthrow(new NoSuchMethodException());
        }
        return null;
    }


    /**
     * Filter the methods by the given predicate
     *
     * @param filter The predicate
     * @return The filtered {@link MethodStream}
     */
    public MethodStream filter(final Predicate<MethodWrapper> filter) {
        this.methods.removeIf(filter.negate());
        return this;
    }

    /**
     * Filter the methods by the given name
     *
     * @param name The name
     * @return The filtered {@link MethodStream}
     */
    public MethodStream filter(final String name) {
        return this.filter(method -> method.name().equals(name));
    }

    /**
     * Filter the methods by the given parameter types
     *
     * @param parameterTypes The parameter types
     * @return The filtered {@link MethodStream}
     */
    public MethodStream filter(final Class<?>... parameterTypes) {
        return this.filter(method -> Arrays.equals(method.parameterTypes(), parameterTypes));
    }

    /**
     * Filter out all static/non-static methods
     *
     * @param isStatic Whether the method should be static or not
     * @return The filtered {@link MethodStream}
     */
    public MethodStream filter(final boolean isStatic) {
        return this.filter(method -> method.modifier().isStatic() == isStatic);
    }


    /**
     * Get an iterator of the {@link MethodWrapper} instances
     */
    public Iterator<MethodWrapper> iterator() {
        return this.methods.iterator();
    }

    /**
     * Get the java stream of the {@link MethodWrapper} instances
     */
    public Stream<MethodWrapper> jstream() {
        return this.methods.stream();
    }

    /**
     * Loop through all {@link MethodWrapper} instances
     *
     * @param consumer The consumer
     * @return The {@link MethodStream}
     */
    public MethodStream forEach(final Consumer<MethodWrapper> consumer) {
        this.methods.forEach(consumer);
        return this;
    }

}
