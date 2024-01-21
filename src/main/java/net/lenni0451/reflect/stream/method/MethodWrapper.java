package net.lenni0451.reflect.stream.method;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.general.ModifierWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A wrapper of the {@link Method} class for easy access to all required methods.
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
     * @return The parent method stream
     */
    public MethodStream parent() {
        return this.parent;
    }

    /**
     * @return The underlying method
     */
    public Method raw() {
        return this.method;
    }

    /**
     * @return The name of the method
     */
    public String name() {
        return this.method.getName();
    }

    /**
     * @return The return type of the method
     */
    public Class<?> returnType() {
        return this.method.getReturnType();
    }

    /**
     * @return The parameter types of the method
     */
    public Class<?>[] parameterTypes() {
        return this.method.getParameterTypes();
    }

    /**
     * @return The amount of parameters of the method
     */
    public int parameterCount() {
        return this.method.getParameterCount();
    }

    /**
     * @return The owner (declaring) class of the method
     */
    public Class<?> owner() {
        return this.method.getDeclaringClass();
    }

    /**
     * @return The {@link ModifierWrapper} of the method
     */
    public ModifierWrapper modifier() {
        return this.modifier;
    }

    /**
     * @return A stream of all annotations of the method
     */
    public Stream<Annotation> annotations() {
        return Arrays.stream(this.method.getDeclaredAnnotations());
    }


    /**
     * Invoke the method without any arguments.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param <T> The type of the result
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public <T> T invoke() {
        return this.invokeArgs();
    }

    /**
     * Invoke the method without any arguments and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public RStream stream() {
        return RStream.of(this.<Object>invoke());
    }

    /**
     * Invoke the method without any arguments and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param clazz The class used for the stream
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public RStream stream(final Class<?> clazz) {
        return RStream.of(clazz, this.invoke());
    }

    /**
     * Invoke the method with the given arguments.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param args The arguments to pass to the method
     * @param <T>  The type of the result
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public <T> T invokeArgs(final Object... args) {
        if (!this.modifier.isStatic() && this.parent.parent().instance() == null) throw new IllegalStateException("Can not invoke non-static method if no instance is provided");
        return Methods.invoke(this.parent.parent().instance(), this.method, args);
    }

    /**
     * Invoke the method with the given arguments and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param args The arguments to pass to the method
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public RStream streamArgs(final Object... args) {
        return RStream.of(this.<Object>invokeArgs(args));
    }

    /**
     * Invoke the method with the given arguments and wrap it in a new {@link RStream}.<br>
     * The cached instance of the owner will be used if required.
     *
     * @param clazz The class used for the stream
     * @param args  The arguments to pass to the method
     * @return The result of the invocation or null if the method is void
     * @throws IllegalStateException If the method is not static and no instance is cached
     */
    public RStream streamArgs(final Class<?> clazz, final Object... args) {
        return RStream.of(clazz, this.invokeArgs(args));
    }

    /**
     * Invoke the method of the given instance with the given arguments.
     *
     * @param instance The instance to invoke the method on
     * @param args     The arguments to pass to the method
     * @param <T>      The type of the result
     * @return The result of the invocation or null if the method is void
     */
    public <T> T invokeInstance(final Object instance, final Object... args) {
        return Methods.invoke(instance, this.method, args);
    }

    /**
     * Invoke the method of the given instance with the given arguments and wrap it in a new {@link RStream}.
     *
     * @param instance The instance to invoke the method on
     * @param args     The arguments to pass to the method
     * @return The result of the invocation or null if the method is void
     */
    public RStream streamInstance(final Object instance, final Object... args) {
        return RStream.of(this.<Object>invokeInstance(instance, args));
    }

    /**
     * Invoke the method of the given instance with the given arguments and wrap it in a new {@link RStream}.
     *
     * @param clazz    The class used for the stream
     * @param instance The instance to invoke the method on
     * @param args     The arguments to pass to the method
     * @return The result of the invocation or null if the method is void
     */
    public RStream streamInstance(final Class<?> clazz, final Object instance, final Object... args) {
        return RStream.of(clazz, this.invokeInstance(instance, args));
    }

}
