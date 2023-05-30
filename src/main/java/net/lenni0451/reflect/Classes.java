package net.lenni0451.reflect;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.JVMConstants.METHOD_Class_getDeclaredClasses0;
import static net.lenni0451.reflect.JavaBypass.UNSAFE;

/**
 * This class contains some useful methods for working with classes.
 */
public class Classes {

    /**
     * Get all declared classes of a class.<br>
     * An empty array will be returned if the method could not be invoked.
     *
     * @param clazz The class to get the declared classes from
     * @return An array of all declared classes of the class
     */
    public static Class<?>[] getDeclaredClasses(final Class<?> clazz) {
        try {
            Method getDeclaredClasses0 = Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredClasses0);
            return Methods.invoke(clazz, getDeclaredClasses0);
        } catch (Throwable ignored) {
        }
        return new Class<?>[0];
    }

    /**
     * Get a declared class of a class by its simple name.
     *
     * @param clazz      The class to get the declared class from
     * @param simpleName The simple name of the class
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> getDeclaredClass(final Class<?> clazz, final String simpleName) {
        for (Class<?> c : getDeclaredClasses(clazz)) {
            if (c.getSimpleName().equals(simpleName)) return c;
        }
        return null;
    }


    /**
     * Get a class by its name.<br>
     * A wrapper for {@link Class#forName(String)} returning null instead of throwing an exception.
     *
     * @param name The name of the class
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * The {@code initialize} parameter is set to true.
     *
     * @param name   The name of the class
     * @param loader The class loader to get the class from
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name, final ClassLoader loader) {
        return byName(name, true, loader);
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * A wrapper for {@link Class#forName(String, boolean, ClassLoader)} returning null instead of throwing an exception.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loader     The class loader to get the class from
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Get a class by its name.<br>
     * A wrapper for {@link Class#forName(String)} sneaky throwing an exception.
     *
     * @param name The name of the class
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nullable
    public static Class<?> forName(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * The {@code initialize} parameter is set to true.
     *
     * @param name   The name of the class
     * @param loader The class loader to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nullable
    public static Class<?> forName(final String name, final ClassLoader loader) {
        return forName(name, true, loader);
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * A wrapper for {@link Class#forName(String, boolean, ClassLoader)} sneaky throwing an exception.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loader     The class loader to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nullable
    public static Class<?> forName(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

}
