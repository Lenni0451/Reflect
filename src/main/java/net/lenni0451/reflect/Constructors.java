package net.lenni0451.reflect;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.JavaBypass.UNSAFE;

/**
 * This class contains some useful methods for working with constructors.
 */
public class Constructors {

    /**
     * Get all declared constructors of a class.<br>
     * The reflection filter of the class will be ignored.<br>
     * An empty array will be returned if the method could not be invoked.
     *
     * @param clazz The class to get the constructors from
     * @param <T>   The type of the class
     * @return An array of all declared constructors of the class
     */
    public static <T> Constructor<T>[] getDeclaredConstructors(final Class<T> clazz) {
        try {
            Method getDeclaredConstructors0 = Class.class.getDeclaredMethod("getDeclaredConstructors0", boolean.class);
            return Methods.invoke(clazz, getDeclaredConstructors0, false);
        } catch (Throwable ignored) {
        }
        return new Constructor[0];
    }

    /**
     * Get a declared constructor of a class by its parameter types.<br>
     * The reflection filter of the class will be ignored.
     *
     * @param clazz          The class to get the constructor from
     * @param parameterTypes The parameter types of the constructor
     * @param <T>            The type of the class
     * @return The constructor or null if it doesn't exist
     */
    @Nullable
    public static <T> Constructor<T> getDeclaredConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        for (Constructor<T> constructor : getDeclaredConstructors(clazz)) if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) return constructor;
        return null;
    }


    /**
     * Invoke a constructor without any checks.<br>
     * The constructor does not have to be accessible.
     *
     * @param constructor The constructor to invoke
     * @param args        The arguments to pass to the constructor
     * @param <T>         The type of the class
     * @return The instance of the class
     */
    public static <T> T invoke(final Constructor<T> constructor, final Object... args) {
        try {
            return (T) TRUSTED_LOOKUP.unreflectConstructor(constructor).asSpreader(Object[].class, args.length).invoke(args);
        } catch (Throwable t) {
            UNSAFE.throwException(new RuntimeException("Unable to invoke constructor", t));
        }
        return null;
    }

}
