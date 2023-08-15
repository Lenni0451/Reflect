package net.lenni0451.reflect.stream.method;

import net.lenni0451.reflect.JavaBypass;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.stream.RStream;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
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
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final String name) {
        for (MethodWrapper method : this.methods) {
            if (method.name().equals(name)) return method;
        }
        JavaBypass.UNSAFE.throwException(new NoSuchMethodException());
        return null;
    }

    /**
     * Get a method by the given parameter types.<br>
     * If there are multiple methods with the same parameter types the first one will be returned.
     *
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(@Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (MethodWrapper method : this.methods) {
                if (method.parameterCount() == 0) return method;
            }
        } else {
            for (MethodWrapper method : this.methods) {
                if (Arrays.equals(method.parameterTypes(), parameterTypes)) return method;
            }
        }
        JavaBypass.UNSAFE.throwException(new NoSuchMethodException());
        return null;
    }

    /**
     * Get a method by the given name and parameter types.
     *
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method wrapper
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final String name, @Nullable final Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            for (MethodWrapper method : this.methods) {
                if (method.name().equals(name) && method.parameterCount() == 0) return method;
            }
        } else {
            for (MethodWrapper method : this.methods) {
                if (method.name().equals(name) && Arrays.equals(method.parameterTypes(), parameterTypes)) return method;
            }
        }
        JavaBypass.UNSAFE.throwException(new NoSuchMethodException());
        return null;
    }

    /**
     * Get a method by the given index.<br>
     * The index is the position of the method in the stream.
     *
     * @param index The index of the method
     * @return The method wrapper
     * @throws NoSuchMethodException If the method doesn't exist
     */
    public MethodWrapper by(final int index) {
        try {
            return this.methods.get(index);
        } catch (IndexOutOfBoundsException e) {
            JavaBypass.UNSAFE.throwException(new NoSuchMethodException());
        }
        return null;
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
     * @return An iterator of method wrappers
     */
    public Iterator<MethodWrapper> iterator() {
        return this.methods.iterator();
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

}
