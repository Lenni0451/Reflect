package net.lenni0451.reflect.stream.constructor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.reflect.Constructor;

/**
 * Wrap a {@link Constructor} for easy access
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
     * Get the parent {@link ConstructorStream}
     */
    public ConstructorStream parent() {
        return this.parent;
    }

    /**
     * Get the raw {@link Constructor}
     */
    public Constructor<?> raw() {
        return this.constructor;
    }

    /**
     * Get the parameter types of the {@link Constructor}
     */
    public Class<?>[] parameterTypes() {
        return this.constructor.getParameterTypes();
    }

    /**
     * Get the owner {@link Class} of the {@link Constructor}
     */
    public Class<?> owner() {
        return this.constructor.getDeclaringClass();
    }

    /**
     * Get the {@link ModifierWrapper} of the {@link Constructor}
     */
    public ModifierWrapper modifier() {
        return this.modifier;
    }


    /**
     * Invoke the {@link Constructor} with the given arguments
     *
     * @param args The arguments to pass to the {@link Constructor}
     * @return The result of the invocation
     */
    public <T> T newInstance(final Object... args) {
        return (T) Constructors.invoke(this.constructor, args);
    }

}
