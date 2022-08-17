package net.lenni0451.reflect.stream.method;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.reflect.Method;

/**
 * Wrap a {@link Method} for easy access
 */
public class MethodWrapper {

    private final MethodStream parent;
    private final Method method;
    private final ModifierWrapper modifier;

    public MethodWrapper(final MethodStream parent, final Method method) {
        this.parent = parent;
        this.method = method;
        this.modifier = new ModifierWrapper(method.getModifiers());
    }

    /**
     * Get the parent {@link MethodStream}
     */
    public MethodStream parent() {
        return this.parent;
    }

    /**
     * Get the raw {@link Method}
     */
    public Method raw() {
        return this.method;
    }

    /**
     * Get the name of the {@link Method}
     */
    public String name() {
        return this.method.getName();
    }

    /**
     * Get the return type of the {@link Method}
     */
    public Class<?> returnType() {
        return this.method.getReturnType();
    }

    /**
     * Get the parameter types of the {@link Method}
     */
    public Class<?>[] parameterTypes() {
        return this.method.getParameterTypes();
    }

    /**
     * Get the owner {@link Class} of the {@link Method}
     */
    public Class<?> owner() {
        return this.method.getDeclaringClass();
    }

    /**
     * Get the {@link ModifierWrapper} of the {@link Method}
     */
    public ModifierWrapper modifier() {
        return this.modifier;
    }


    /**
     * Invoke the {@link Method} without any args
     *
     * @return The result of the invocation
     */
    public <T> T invoke() {
        return this.invokeArgs();
    }

    /**
     * Invoke the {@link Method} with the given arguments
     *
     * @param args The arguments to pass to the {@link Method}
     * @return The result of the invocation
     */
    public <T> T invokeArgs(final Object... args) {
        if (!this.modifier.isStatic() && this.parent.parent().instance() == null) throw new IllegalStateException("Can not invoke non-static method if no instance is provided");
        return Methods.invoke(this.parent.parent().instance(), this.method, args);
    }

    /**
     * Invoke the {@link Method} with the give arguments and instance
     *
     * @param instance The instance of the owner
     * @param args     The arguments to pass to the {@link Method}
     * @return The result of the invocation
     */
    public <T> T invokeInstance(final Object instance, final Object... args) {
        return Methods.invoke(instance, this.method, args);
    }

}
