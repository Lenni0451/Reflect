package net.lenni0451.reflect;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.utils.FieldInitializer.ThrowingSupplier.getFirst;
import static net.lenni0451.reflect.utils.FieldInitializer.*;

/**
 * The main class for bypassing java restrictions.<br>
 * This class contains the unsafe instance and the trusted lookup instance used for everything else.
 */
public class JavaBypass {

    /**
     * The instance of the unsafe class.
     */
    public static final Unsafe UNSAFE = getUnsafe();
    /**
     * The instance of the trusted lookup.
     */
    public static final MethodHandles.Lookup TRUSTED_LOOKUP = getTrustedLookup();
    /**
     * The instance of the internal unsafe class.
     */
    @Nullable
    public static final Object INTERNAL_UNSAFE = getInternalUnsafe();

    /**
     * Get the unsafe instance.<br>
     * You should use the static instance {@link #UNSAFE} instead.
     *
     * @return The unsafe instance
     * @throws IllegalStateException If the unsafe instance could not be gotten
     */
    public static Unsafe getUnsafe() {
        return process(
                () -> reqInit(
                        () -> {
                            for (Field field : Unsafe.class.getDeclaredFields()) {
                                if (field.getType().equals(Unsafe.class)) {
                                    field.setAccessible(true);
                                    return (Unsafe) field.get(null);
                                }
                            }
                            return null;
                        },
                        () -> new IllegalStateException("Unsafe field not found or was null")
                ),
                cause -> new IllegalStateException("Unable to get unsafe instance", cause)
        );
    }

    /**
     * Get the trusted lookup instance.<br>
     * You should use the static instance {@link #TRUSTED_LOOKUP} instead.
     *
     * @return The trusted lookup instance
     * @throws IllegalStateException If the trusted lookup instance could not be gotten
     */
    public static MethodHandles.Lookup getTrustedLookup() {
        return process(
                () -> reqInit(
                        getFirst(() -> {
                            MethodHandles.lookup(); //Load class before getting the trusted lookup
                            Field lookupField = MethodHandles.Lookup.class.getDeclaredField(FIELD_MethodHandles_Lookup_IMPL_LOOKUP);
                            long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
                            return (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(lookupField), lookupFieldOffset);
                        }, () -> {
                            MethodHandles.Lookup lookup = (MethodHandles.Lookup) ReflectionFactory.getReflectionFactory()
                                    .newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class))
                                    .newInstance(MethodHandles.Lookup.class);
                            return (MethodHandles.Lookup) lookup.findStaticGetter(MethodHandles.Lookup.class, FIELD_MethodHandles_Lookup_IMPL_LOOKUP, MethodHandles.Lookup.class).invokeExact();
                        }),
                        () -> new IllegalStateException("Lookup field was null")
                ),
                cause -> new IllegalStateException("Unable to get trusted lookup instance", cause)
        );
    }

    /**
     * Get the internal unsafe instance.<br>
     * You should use the static instance {@link #INTERNAL_UNSAFE} instead.<br>
     * The internal unsafe was added in Java 11 and has more low level access.
     *
     * @return The internal unsafe instance
     */
    @Nullable
    public static Object getInternalUnsafe() {
        return process(
                () -> condReqInit(
                        () -> Class.forName(CLASS_INTERNAL_Unsafe),
                        unsafeClass -> {
                            for (Field field : unsafeClass.getDeclaredFields()) {
                                if (field.getType().equals(unsafeClass)) return TRUSTED_LOOKUP.unreflectGetter(field).invoke();
                            }
                            return null;
                        },
                        () -> new IllegalStateException("Internal unsafe field not found or was null")
                ),
                cause -> new IllegalStateException("Unable to get internal unsafe instance", cause)
        );
    }

    /**
     * Clear the reflection filter maps.<br>
     * This will allow you to access all fields and methods of a class.
     *
     * @throws ClassNotFoundException If the Reflection class is not found
     */
    public static void clearReflectionFilter() throws ClassNotFoundException {
        Class<?> reflectionClass;
        try {
            reflectionClass = Class.forName(CLASS_INTERNAL_Reflection);
        } catch (Throwable t) {
            reflectionClass = Class.forName(CLASS_SUN_Reflection);
        }

        Fields.setObject(null, Fields.getDeclaredField(reflectionClass, FIELD_Reflection_fieldFilterMap), null);
        Fields.setObject(null, Fields.getDeclaredField(reflectionClass, FIELD_Reflection_methodFilterMap), null);
    }

}
