package net.lenni0451.reflect;

import java.lang.reflect.Method;

import static net.lenni0451.reflect.JavaBypass.UNSAFE;

public class Classes {

    /**
     * Get all declared classes of a class
     *
     * @param clazz The class to get the declared classes of
     * @return An array of all declared classes of the class
     */
    public static Class<?>[] getDeclaredClasses(final Class<?> clazz) {
        try {
            Method getDeclaredClasses0 = Class.class.getDeclaredMethod("getDeclaredClasses0");
            return Methods.invoke(clazz, getDeclaredClasses0, false);
        } catch (Throwable ignored) {
        }
        return new Class<?>[0];
    }

    /**
     * Get a declared class by its name
     *
     * @param clazz      The class to get the declared class of
     * @param simpleName The name of the class
     * @return The declared class or null if not found
     */
    public static Class<?> getDeclaredClass(final Class<?> clazz, final String simpleName) {
        for (Class<?> c : getDeclaredClasses(clazz)) if (c.getSimpleName().equals(simpleName)) return c;
        return null;
    }


    /**
     * Get a class by its name<br>
     * Basically a wrapper for {@link Class#forName(String)} but without direct exceptions
     *
     * @param name The name of the class
     * @return The class
     */
    public static Class<?> forName(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

    /**
     * Get a class by its name from a given class loader
     *
     * @param loader The class loader to get the class from
     * @param name   The name of the class
     * @return The class
     */
    public static Class<?> forName(final ClassLoader loader, final String name) {
        return forName(loader, name, true);
    }

    /**
     * Get a class by its name from a given class loader<br>
     * Basically a wrapper for {@link Class#forName(String, boolean, ClassLoader)} but without direct exceptions
     *
     * @param loader     The class loader to get the class from
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @return The class
     */
    public static Class<?> forName(final ClassLoader loader, final String name, final boolean initialize) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

}
