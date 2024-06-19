package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.bytecode.BytecodeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProxyUtils {

    public static void verifySuperClass(final Class<?> clazz) {
        if (Modifier.isPrivate(clazz.getModifiers())) throw new IllegalArgumentException("Cannot create a proxy for a private class");
        if (Modifier.isFinal(clazz.getModifiers())) throw new IllegalArgumentException("Cannot create a proxy for a final class");
        if (getPublicConstructors(clazz).length == 0) throw new IllegalArgumentException("Cannot create a proxy for a class without any public constructors");
    }

    public static Constructor<?>[] getPublicConstructors(final Class<?> clazz) {
        List<Constructor<?>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) continue;
            constructors.add(constructor);
        }
        return constructors.toArray(new Constructor<?>[0]);
    }

    public static Method[] getOverridableMethod(final Class<?> superClass, final Class<?>[] interfaces) {
        Map<String, Method> methods = new HashMap<>();
        if (superClass != null) getOverridableMethod(superClass, methods);
        if (interfaces != null) {
            for (Class<?> inter : interfaces) getOverridableMethod(inter, methods);
        }
        return methods.values().toArray(new Method[0]);
    }

    public static void getOverridableMethod(final Class<?> clazz, final Map<String, Method> methods) {
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

}
