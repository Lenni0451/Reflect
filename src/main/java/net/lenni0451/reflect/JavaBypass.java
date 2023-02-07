package net.lenni0451.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

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
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
            return (MethodHandles.Lookup) UNSAFE.getObject(MethodHandles.Lookup.class, lookupFieldOffset);
        } catch (Throwable ignored) {
        }
        throw new IllegalStateException("Unable to get trusted lookup");
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
            reflectionClass = Class.forName("jdk.internal.reflect.Reflection");
        } catch (Throwable t) {
            reflectionClass = Class.forName("sun.reflect.Reflection");
        }

        Fields.setObject(null, Fields.getDeclaredField(reflectionClass, "fieldFilterMap"), null);
        Fields.setObject(null, Fields.getDeclaredField(reflectionClass, "methodFilterMap"), null);
    }

}
