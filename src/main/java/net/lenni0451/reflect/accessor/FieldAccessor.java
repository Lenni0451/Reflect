package net.lenni0451.reflect.accessor;

import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.init;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

/**
 * Generate a getter/setter instance for a field.<br>
 * This can be used to get/set private fields without having to resort to reflection.<br>
 * The implementation is generated using the internal ASM.
 */
public class FieldAccessor {

    private static final Class<?> FIELD_ACCESSOR_IMPL = reqInit(
            () -> Class.forName("net.lenni0451.reflect.accessor.internal.ASMFieldAccessor"),
            () -> new ClassNotFoundException("Unable to find FieldAccessor implementation class")
    );
    private static final MethodHandle MAKE_SETTER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeSetter", MethodType.methodType(Object.class, Class.class, Object.class, Field.class))
    );
    private static final MethodHandle MAKE_DYNAMIC_SETTER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeDynamicSetter", MethodType.methodType(Object.class, Class.class, Field.class))
    );
    private static final MethodHandle MAKE_GETTER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeGetter", MethodType.methodType(Object.class, Class.class, Object.class, Field.class))
    );
    private static final MethodHandle MAKE_DYNAMIC_GETTER = init(
            () -> TRUSTED_LOOKUP.findStatic(FIELD_ACCESSOR_IMPL, "makeDynamicGetter", MethodType.methodType(Object.class, Class.class, Field.class))
    );

    /**
     * Create a new setter instance for the given field.<br>
     * The invoker class must have a method with one parameter and no return type.<br>
     * Super types of the parameter type are also allowed.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the field is in
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The setter instance
     */
    @SneakyThrows
    public static <I> I makeSetter(@Nonnull final Class<?> invokerClass, final Object instance, @Nonnull final Field field) {
        return (I) MAKE_SETTER.invokeExact(invokerClass, instance, field);
    }

    /**
     * Create a new dynamic setter instance for the given field.<br>
     * The invoker class must have a method with two parameters and no return type.<br>
     * Super types of the parameter types are also allowed.<br>
     * Only non-static fields can be used.
     *
     * @param invokerClass The invoker interface class
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The dynamic setter instance
     */
    @SneakyThrows
    public static <I> I makeDynamicSetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        return (I) MAKE_DYNAMIC_SETTER.invokeExact(invokerClass, field);
    }

    /**
     * Create a new getter instance for the given field.<br>
     * The invoker class must have a method with no parameters and right return type.<br>
     * Super types of the return type are also allowed.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the field is in
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The getter instance
     */
    @SneakyThrows
    public static <I> I makeGetter(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Field field) {
        return (I) MAKE_GETTER.invokeExact(invokerClass, instance, field);
    }

    /**
     * Create a new dynamic getter instance for the given field.<br>
     * The invoker class must have a method with one parameter and right return type.<br>
     * Super types of the parameter type and return type are also allowed.<br>
     * Only non-static fields can be used.
     *
     * @param invokerClass The invoker interface class
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The dynamic getter instance
     */
    @SneakyThrows
    public static <I> I makeDynamicGetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        return (I) MAKE_DYNAMIC_GETTER.invokeExact(invokerClass, field);
    }

}
