package net.lenni0451.reflect.proxy.impl;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

/**
 * Util methods invoked by the proxy classes at runtime.
 */
@ApiStatus.Internal
public class ProxyRuntime {

    /**
     * Invoked in the static initializer of the proxy method class.<br>
     * This will get the required method handles for the method.
     *
     * @param owner      The owner of the method
     * @param name       The name of the method
     * @param parameters The parameter types of the method
     * @param returnType The return type of the method
     * @return The method handles for the method
     * @throws NoSuchMethodException  If the method does not exist
     * @throws IllegalAccessException If the method is not accessible
     */
    public static MethodHandle[] getMethodHandles(final Class<?> owner, final String name, final Class<?>[] parameters, final Class<?> returnType) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle[] methodHandles = new MethodHandle[2];
        methodHandles[0] = TRUSTED_LOOKUP.findVirtual(owner, name, MethodType.methodType(returnType, parameters));
        try {
            methodHandles[1] = TRUSTED_LOOKUP.findSpecial(owner, name, MethodType.methodType(returnType, parameters), owner);
        } catch (Throwable ignored) {
        }
        return methodHandles;
    }

    /**
     * Invoked in the proxy methods.
     *
     * @param clazz    The class of the proxy method to instantiate
     * @param instance The instance of the proxy
     * @param method   The method to proxy
     * @return The instantiated proxy method
     * @throws NoSuchMethodException     If the constructor does not exist
     * @throws InvocationTargetException If the constructor throws an exception
     * @throws InstantiationException    If the class is abstract
     * @throws IllegalAccessException    If the constructor is not accessible
     */
    public static ProxyMethod instantiateProxyMethod(final Class<ProxyMethod> clazz, final Object instance, final Method method) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = clazz.getDeclaredConstructor(instance.getClass(), Method.class);
        constructor.setAccessible(true);
        return (ProxyMethod) constructor.newInstance(instance, method);
    }

}
