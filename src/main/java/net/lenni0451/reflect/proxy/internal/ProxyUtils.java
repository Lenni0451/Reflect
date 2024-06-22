package net.lenni0451.reflect.proxy.internal;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.bytecode.BytecodeUtils;
import net.lenni0451.reflect.proxy.impl.Proxy;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utils for creating proxy classes.
 */
@ApiStatus.Internal
public class ProxyUtils {

    public static void verifySuperClass(final Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) throw new IllegalArgumentException("The super class must be public");
        if (clazz.isInterface()) throw new IllegalArgumentException("The super class must be a class");
        if (Modifier.isFinal(clazz.getModifiers())) throw new IllegalArgumentException("The super class must not be final");
//        if (getPublicConstructors(clazz).length == 0) throw new IllegalArgumentException("The super class must have at least one public constructor");
    }

    public static void verifyInterface(final Class<?> clazz) {
        if (clazz == Proxy.class) throw new IllegalArgumentException("The 'Proxy' interface is not allowed as interface");
        if (!Modifier.isPublic(clazz.getModifiers())) throw new IllegalArgumentException("The interface must be public");
        if (!clazz.isInterface()) throw new IllegalArgumentException("The interface must be an interface");
    }

    public static Constructor<?>[] getPublicConstructors(final Class<?> clazz) {
        List<Constructor<?>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) continue;
            constructors.add(constructor);
        }
        return constructors.toArray(new Constructor<?>[0]);
    }

    public static Method[] getOverridableMethod(final Class<?> superClass, final Class<?>[] interfaces, final Predicate<Method> filter) {
        Map<String, Method> methods = new HashMap<>();
        if (superClass != null) getOverridableMethod(superClass, methods);
        if (interfaces != null) {
            for (Class<?> inter : interfaces) getOverridableMethod(inter, methods);
        }
        return methods.values().stream().filter(filter).toArray(Method[]::new);
    }

    public static void getOverridableMethod(final Class<?> clazz, final Map<String, Method> methods) {
        if (clazz == Proxy.class) return; //Ignore the Proxy interface
        for (Method method : Methods.getDeclaredMethods(clazz)) {
            if (Modifier.isPrivate(method.getModifiers())) continue;
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (Modifier.isFinal(method.getModifiers())) continue;
            if (Modifier.isNative(method.getModifiers())) continue;
            methods.putIfAbsent(method.getName() + BytecodeUtils.desc(method), method);
        }
        if (clazz.getSuperclass() != null) getOverridableMethod(clazz.getSuperclass(), methods);
        for (Class<?> inter : clazz.getInterfaces()) getOverridableMethod(inter, methods);
    }

    public static Method[] mapMethods(final Method[] methods, final Function<Method, Method> methodMapper) {
        Method[] originalMethods = new Method[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Method mappedMethod = methodMapper.apply(method);
            if (method.equals(mappedMethod)) continue;

            if (!mappedMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                throw new IllegalArgumentException("The method '" + method.getName() + "' in class '" + method.getDeclaringClass().getName() + "' is not assignable to the method in the proxy class");
            }
            methods[i] = mappedMethod;
            originalMethods[i] = method;
        }
        return originalMethods;
    }

}
