package net.lenni0451.reflect.accessor;

import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.init;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

/**
 * Generate an invoker interface instance for a method.<br>
 * This can be used to call private methods without having to resort to reflection.<br>
 * The implementation is generated at runtime using the internal ASM.
 */
public class MethodAccessor {

    private static final Class<?> FIELD_ACCESSOR_IMPL = reqInit(
            () -> Class.forName("net.lenni0451.reflect.accessor.internal.ASMMethodAccessor"),
            () -> new ClassNotFoundException("Unable to find MethodAccessor implementation class")
    );
    private static final MethodHandle MAKE_INVOKER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeInvoker", MethodType.methodType(Object.class, Class.class, Object.class, Method.class))
    );
    private static final MethodHandle MAKE_DYNAMIC_INVOKER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeDynamicInvoker", MethodType.methodType(Object.class, Class.class, Method.class))
    );

    /**
     * Create a new invoker instance for the given method.<br>
     * The invoker class must have a method with the right amount of parameters and right return type.<br>
     * Super types of the parameter types/return type are also allowed.<br>
     * The instance parameter is only used if the method is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the method is in
     * @param method       The method to invoke
     * @param <I>          The invoker interface type
     * @return The invoker instance implementation
     */
    @SneakyThrows
    public static <I> I makeInvoker(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Method method) {
        return (I) MAKE_INVOKER.invokeExact(invokerClass, instance, method);
    }

    /**
     * Create a new dynamic invoker instance for the given method.<br>
     * The invoker class must have a method with the right amount of parameters and right return type. A instance parameter is required at the beginning of the parameter list.<br>
     * Super types of the parameter types/return type are also allowed.<br>
     * Only non-static methods can be used.
     *
     * @param invokerClass The invoker interface class
     * @param method       The method to invoke
     * @param <I>          The invoker interface type
     * @return The invoker instance implementation
     */
    @SneakyThrows
    public static <I> I makeDynamicInvoker(@Nonnull final Class<I> invokerClass, @Nonnull final Method method) {
        return (I) MAKE_DYNAMIC_INVOKER.invokeExact(invokerClass, method);
    }

}
