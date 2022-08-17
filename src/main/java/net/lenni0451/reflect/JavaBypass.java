package net.lenni0451.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class JavaBypass {

    public static final Unsafe UNSAFE = getUnsafe();
    public static final MethodHandles.Lookup TRUSTED_LOOKUP = getTrustedLookup();

    /**
     * Get the unsafe instance
     *
     * @return The unsafe instance
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
     * Get the trusted lookup instance
     *
     * @return The trusted lookup instance
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
     * Clear the reflection filter maps
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
