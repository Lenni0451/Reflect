package net.lenni0451.reflect;

import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static net.lenni0451.reflect.JVMConstants.*;

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
        try {
            for (Field field : Unsafe.class.getDeclaredFields()) {
                if (field.getType().equals(Unsafe.class)) {
                    field.setAccessible(true);
                    return (Unsafe) field.get(null);
                }
            }
        } catch (Throwable ignored) {
        }
        throw new IllegalStateException("Unable to get Unsafe instance");
    }

    /**
     * Get the trusted lookup instance.<br>
     * You should use the static instance {@link #TRUSTED_LOOKUP} instead.
     *
     * @return The trusted lookup instance
     * @throws IllegalStateException If the trusted lookup instance could not be gotten
     */
    public static MethodHandles.Lookup getTrustedLookup() {
        try {
            MethodHandles.lookup(); //Load class before getting the trusted lookup
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField(FIELD_MethodHandles_Lookup_IMPL_LOOKUP);
            long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
            return (MethodHandles.Lookup) UNSAFE.getObject(MethodHandles.Lookup.class, lookupFieldOffset);
        } catch (Throwable ignored) {
        }
        throw new IllegalStateException("Unable to get trusted lookup");
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
        try {
            Class<?> unsafeClass = Class.forName(CLASS_INTERNAL_Unsafe);
            for (Field field : unsafeClass.getDeclaredFields()) {
                if (field.getType().equals(unsafeClass)) return TRUSTED_LOOKUP.unreflectGetter(field).invoke();
            }
        } catch (Throwable ignored) {
        }
        return null;
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
