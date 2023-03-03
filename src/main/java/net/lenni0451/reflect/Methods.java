package net.lenni0451.reflect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class contains some useful methods for working with methods.
 */
public class Methods {

    /**
     * Get all declared methods of a class.<br>
     * The reflection filter of the class will be ignored.<br>
     * An empty array will be returned if the method could not be invoked.
     *
     * @param clazz The class to get the methods from
     * @return An array of all declared methods of the class
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz) {
        try {
            Method getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            return Methods.invoke(clazz, getDeclaredMethods0, false);
        } catch (Throwable ignored) {
        }
        return new Method[0];
    }

    /**
     * Get a declared method of a class by its name and parameter types.<br>
     * The reflection filter of the class will be ignored.
     *
     * @param clazz          The class to get the method from
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method or null if it doesn't exist
     */
    @Nullable
    public static Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
        for (Method method : getDeclaredMethods(clazz)) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) return method;
        }
        return null;
    }


    /**
     * Invoke a method without any checks.<br>
     * The method does not have to be accessible.
     *
     * @param instance The instance to invoke the method on
     * @param method   The method to invoke
     * @param args     The arguments to pass to the method
     * @param <T>      The return type of the method
     * @return The return value of the method (null if void)
     * @throws RuntimeException If the method could not be invoked
     */
    public static <T> T invoke(@Nullable final Object instance, final Method method, final Object... args) {
        try {
            if (Modifier.isStatic(method.getModifiers())) return (T) JavaBypass.TRUSTED_LOOKUP.unreflect(method).invokeWithArguments(args);
            else return (T) JavaBypass.TRUSTED_LOOKUP.unreflect(method).bindTo(instance).invokeWithArguments(args);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to invoke method " + method.getName(), t);
        }
    }

    /**
     * Invoke a super method without any checks.<br>
     * The method does not have to be accessible.
     *
     * @param instance   The instance to invoke the method on
     * @param superClass The super class to call the method of
     * @param method     The method to invoke
     * @param args       The arguments to pass to the method
     * @param <I>        The type of the instance
     * @param <S>        The type of the super class
     * @param <T>        The return type of the method
     * @return The return value of the method (null if void)
     * @throws RuntimeException If the method could not be invoked
     */
    public static <I extends S, S, T> T invokeSuper(@Nonnull final I instance, @Nonnull final Class<S> superClass, final Method method, final Object... args) {
        try {
            if (Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Cannot invoke static super method");
            else return (T) JavaBypass.TRUSTED_LOOKUP.unreflectSpecial(method, superClass).bindTo(instance).invokeWithArguments(args);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to invoke method " + method.getName(), t);
        }
    }

}
