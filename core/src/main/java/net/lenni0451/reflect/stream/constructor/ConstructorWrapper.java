package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A wrapper of the {@link Constructor} class for easy access to all required methods.
 */
public class ConstructorWrapper {

    private final ConstructorStream parent;
    private final Constructor<?> constructor;
    private final ModifierWrapper modifier;

    public ConstructorWrapper(final ConstructorStream parent, final Constructor<?> constructor) {
        this.parent = parent;
        this.constructor = constructor;
        this.modifier = new ModifierWrapper(constructor.getModifiers());
    }

    /**
     * @return The parent constructor stream
     */
    public ConstructorStream parent() {
        return this.parent;
    }

    /**
     * @return The underlying constructor
     */
    public Constructor<?> raw() {
        return this.constructor;
    }

    /**
     * @return The parameter types of the constructor
     */
    public Class<?>[] parameterTypes() {
        return this.constructor.getParameterTypes();
    }

    /**
     * @return The amount of parameters of the constructor
     */
    public int parameterCount() {
        return this.constructor.getParameterCount();
    }

    /**
     * @return The owner (declaring) class of the constructor
     */
    public Class<?> owner() {
        return this.constructor.getDeclaringClass();
    }

    /**
     * @return The {@link ModifierWrapper} of the constructor
     */
    public ModifierWrapper modifier() {
        return this.modifier;
    }

    /**
     * @return A stream of all annotations of the constructor
     */
    public Stream<Annotation> annotations() {
        return Arrays.stream(this.constructor.getDeclaredAnnotations());
    }


    /**
     * Create a new instance of the owner class with the given arguments.
     *
     * @param args The arguments to pass to the constructor
     * @param <T>  The type of the owner class
     * @return The new instance
     */
    public <T> T newInstance(final Object... args) {
        return (T) Constructors.invoke(this.constructor, args);
    }

    /**
     * Create a new instance of the owner class with the given arguments and wrap it in a new {@link RStream}.
     *
     * @param args The arguments to pass to the constructor
     * @return The new instance
     */
    public RStream streamInstance(final Object... args) {
        return RStream.of(this.<Object>newInstance(args));
    }

    /**
     * Create a new instance of the owner class with the given arguments and wrap it in a new {@link RStream}.
     *
     * @param clazz The class used for the stream
     * @param args  The arguments to pass to the constructor
     * @return The new instance
     */
    public RStream streamInstance(final Class<?> clazz, final Object... args) {
        return RStream.of(clazz, this.newInstance(args));
    }


    @Override
    public String toString() {
        return this.constructor.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstructorWrapper) return this.constructor.equals(((ConstructorWrapper) obj).constructor);
        else if (obj instanceof Constructor) return this.constructor.equals(obj);
        else return false;
    }

    @Override
    public int hashCode() {
        return this.constructor.hashCode();
    }

}
