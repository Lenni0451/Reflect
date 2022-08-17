package net.lenni0451.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static net.lenni0451.reflect.JavaBypass.UNSAFE;

public class Methods {

    /**
     * Get all declared methods of a class
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
     * Get a declared method of a class by its name and parameter types
     *
     * @param clazz          The class to get the method from
     * @param name           The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method or null if it doesn't exist
     */
    public static Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
        for (Method method : getDeclaredMethods(clazz)) if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) return method;
        return null;
    }


    /**
     * Invoke a method without any checks
     *
     * @param instance The instance to invoke the method on
     * @param method   The method to invoke
     * @param args     The arguments to pass to the method
     * @return The return value of the method (null if void)
     */
    public static <T> T invoke(final Object instance, final Method method, final Object... args) {
        try {
            if (Modifier.isStatic(method.getModifiers())) return (T) JavaBypass.TRUSTED_LOOKUP.unreflect(method).invokeWithArguments(args);
            else return (T) JavaBypass.TRUSTED_LOOKUP.unreflectSpecial(method, method.getDeclaringClass()).bindTo(instance).invokeWithArguments(args);
        } catch (Throwable t) {
            UNSAFE.throwException(new RuntimeException("Unable to invoke method " + method.getName(), t));
        }
        return null;
    }

}
