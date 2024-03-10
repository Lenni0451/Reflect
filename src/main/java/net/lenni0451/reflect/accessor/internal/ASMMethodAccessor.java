package net.lenni0451.reflect.accessor.internal;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.wrapper.ASMWrapper;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.lenni0451.reflect.wrapper.ASMWrapper.*;

public class ASMMethodAccessor {

    public static <I> I makeInvoker(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Method method) {
        String newClassName = slash(method.getDeclaringClass()) + "$MethodInvoker";
        boolean staticMethod = Modifier.isStatic(method.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, method, false);
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        if (staticMethod) {
            ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(opcode("RETURN"));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        } else {
            String instanceType = desc(instance.getClass());
            acc.visitField(opcode("ACC_PRIVATE"), "instance", instanceType, null, null);

            MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "(" + instanceType + ")V", null, null);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitVarInsn(opcode("ALOAD"), 1);
            mv.visitFieldInsn(opcode("PUTFIELD"), newClassName, "instance", instanceType);
            mv.visitInsn(opcode("RETURN"));
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }

        String methodClass = slash(method.getDeclaringClass());
        String methodDesc = desc(method);
        boolean interfaceMethod = Modifier.isInterface(method.getDeclaringClass().getModifiers());
        MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        if (staticMethod) {
            pushArgs(mv, invokerMethod.getParameterTypes(), method.getParameterTypes());
            mv.visitMethodInsn(opcode("INVOKESTATIC"), methodClass, method.getName(), methodDesc, interfaceMethod);
        } else {
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitFieldInsn(opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
            pushArgs(mv, invokerMethod.getParameterTypes(), method.getParameterTypes());
            if (interfaceMethod) {
                mv.visitMethodInsn(opcode("INVOKEINTERFACE"), methodClass, method.getName(), methodDesc, true);
            } else {
                mv.visitMethodInsn(opcode("INVOKEVIRTUAL"), methodClass, method.getName(), methodDesc, false);
            }
        }
        if (!method.getReturnType().equals(invokerMethod.getReturnType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
        mv.visitInsn(getReturnOpcode(invokerMethod.getReturnType()));
        mv.visitMaxs(invokerMethod.getParameterCount() + 1, invokerMethod.getParameterCount() + 1);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(method.getDeclaringClass());
        if (Modifier.isStatic(method.getModifiers())) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (I) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (I) Constructors.invoke(constructor, instance);
        }
    }

    public static <I> I makeDynamicInvoker(@Nonnull final Class<I> invokerClass, @Nonnull final Method method) {
        if (Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Dynamic invoker can only be used for non-static methods");
        String newClassName = slash(method.getDeclaringClass()) + "$DynamicMethodInvoker";
        Method invokerMethod = findInvokerMethod(invokerClass, method, true);
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
        mv.visitVarInsn(opcode("ALOAD"), 0);
        mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        pushArgs(mv, invokerMethod.getParameterTypes(), prepend(method.getParameterTypes(), method.getDeclaringClass()));
        if (Modifier.isInterface(method.getDeclaringClass().getModifiers())) {
            mv.visitMethodInsn(opcode("INVOKEINTERFACE"), slash(method.getDeclaringClass()), method.getName(), desc(method), true);
        } else {
            mv.visitMethodInsn(opcode("INVOKEVIRTUAL"), slash(method.getDeclaringClass()), method.getName(), desc(method), false);
        }
        if (!method.getReturnType().equals(invokerMethod.getReturnType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
        mv.visitInsn(getReturnOpcode(invokerMethod.getReturnType()));
        mv.visitMaxs(invokerMethod.getParameterCount() + 1, invokerMethod.getParameterCount() + 1);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(method.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (I) Constructors.invoke(constructor);
    }

    private static Method findInvokerMethod(final Class<?> invokerClass, final Method method, final boolean requireInstance) {
        if (!Modifier.isInterface(invokerClass.getModifiers())) throw new IllegalArgumentException("The invoker class must be an interface");

        int abstractMethods = 0;
        Method matched = null;
        for (Method invokerMethod : Methods.getDeclaredMethods(invokerClass)) {
            if (!Modifier.isAbstract(invokerMethod.getModifiers())) continue;
            if (++abstractMethods > 1) throw new IllegalArgumentException("The invoker class must only have one abstract method");
            if (invokerMethod.getParameterCount() != method.getParameterCount() + (requireInstance ? 1 : 0)) {
                throw new IllegalArgumentException("The invoker method must have " + (method.getParameterCount() + (requireInstance ? 1 : 0)) + " parameters");
            }
            if (!invokerMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                throw new IllegalArgumentException("The invoker method return type must be of type " + method.getReturnType().getName());
            }

            Class<?>[] invokerParameterTypes = invokerMethod.getParameterTypes();
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            for (int i = 0; i < invokerParameterTypes.length; i++) {
                Class<?> invokerParameterType = invokerParameterTypes[i];
                Class<?> methodParameterType = (requireInstance && i == 0) ? method.getDeclaringClass() : methodParameterTypes[i - (requireInstance ? 1 : 0)];
                if (invokerParameterType.isAssignableFrom(methodParameterType)) continue;
                throw new IllegalArgumentException("The invoker method parameter " + i + " must be of type " + methodParameterType);
            }
            matched = invokerMethod;
        }
        if (matched == null) throw new IllegalArgumentException("Could not find a valid invoker method for: " + method);
        return matched;
    }

    private static void pushArgs(final MethodVisitorAccess mv, final Class<?>[] supplied, final Class<?>[] target) {
        int stack = 1;
        for (int i = 0; i < supplied.length; i++) {
            Class<?> suppliedType = supplied[i];
            Class<?> targetType = target[i];
            mv.visitVarInsn(getLoadOpcode(suppliedType), stack);
            if (!suppliedType.equals(targetType)) mv.visitTypeInsn(opcode("CHECKCAST"), slash(targetType));
            stack += getStackSize(suppliedType);
        }
    }

    private static int getStackSize(final Class<?> clazz) {
        if (long.class.equals(clazz) || double.class.equals(clazz)) return 2;
        return 1;
    }

    private static Class<?>[] prepend(final Class<?>[] classes, final Class<?> other) {
        Class<?>[] newClasses = new Class<?>[classes.length + 1];
        newClasses[0] = other;
        System.arraycopy(classes, 0, newClasses, 1, classes.length);
        return newClasses;
    }

}
