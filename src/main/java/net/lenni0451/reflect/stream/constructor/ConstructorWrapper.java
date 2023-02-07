package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.reflect.Constructor;

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
     * Create a new instance of the owner class with the given arguments.
     *
     * @param args The arguments to pass to the constructor
     * @param <T>  The type of the owner class
     * @return The new instance
     */
    public <T> T newInstance(final Object... args) {
        return (T) Constructors.invoke(this.constructor, args);
    }

}
