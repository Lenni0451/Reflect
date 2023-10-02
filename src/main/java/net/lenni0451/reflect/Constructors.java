package net.lenni0451.reflect;

import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static net.lenni0451.reflect.JVMConstants.METHOD_Class_getDeclaredConstructors0;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

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
     * @throws MethodNotFoundException If the {@link Class} internal {@code getDeclaredConstructors0} method could not be found
     */
    public static <T> Constructor<T>[] getDeclaredConstructors(final Class<T> clazz) {
        try {
            if (JVMConstants.OPENJ9_RUNTIME) {
                Method getDeclaredConstructorsImpl = Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredConstructors0);
                if (getDeclaredConstructorsImpl == null) throw new NoSuchMethodException();
                return Methods.invoke(clazz, getDeclaredConstructorsImpl);
            } else {
                Method getDeclaredConstructors0 = Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredConstructors0, boolean.class);
                if (getDeclaredConstructors0 == null) throw new NoSuchMethodException();
                return Methods.invoke(clazz, getDeclaredConstructors0, false);
            }
        } catch (NoSuchMethodException e) {
            throw new MethodNotFoundException(Class.class.getName(), METHOD_Class_getDeclaredConstructors0, JVMConstants.OPENJ9_RUNTIME ? "" : "boolean");
        }
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
        for (Constructor<T> constructor : getDeclaredConstructors(clazz)) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) return constructor;
        }
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
     * @throws RuntimeException If the constructor could not be invoked
     */
    public static <T> T invoke(final Constructor<T> constructor, final Object... args) {
        try {
            return (T) TRUSTED_LOOKUP.unreflectConstructor(constructor).asSpreader(Object[].class, args.length).invoke(args);
        } catch (Throwable t) {
            throw new ConstructorInvocationException(constructor).cause(t);
        }
    }

}
