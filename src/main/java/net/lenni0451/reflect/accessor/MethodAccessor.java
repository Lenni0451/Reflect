package net.lenni0451.reflect.accessor;

import net.lenni0451.reflect.ASMAccess;
import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static net.lenni0451.reflect.ASMAccess.*;

/**
 * Generate an invoker interface instance for a method.<br>
 * This can be used to call private methods without having to resort to reflection.<br>
 * The implementation is generated at runtime using the internal ASM.
 */
public class MethodAccessor {

    /**
     * Create a new invoker instance for the given method.<br>
     * The invoker class must have a method with the right amount of parameters and right return type.<br>
     * Super types of the parameter types/return type are also allowed.<br>
     * The instance parameter is only used if the method is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the method is in
     * @param method       The method to invoke
     * @param <I>          The invoker interface type
     * @return The invoker instance implementation
     */
    public static <I> I makeInvoker(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Method method) {
        String newClassName = dash(method.getDeclaringClass()) + "$MethodInvoker";
        boolean staticMethod = Modifier.isStatic(method.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, method, false);
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{dash(invokerClass)});

        if (staticMethod) {
            MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
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

        String methodClass = dash(method.getDeclaringClass());
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
        if (!method.getReturnType().equals(invokerMethod.getReturnType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(invokerMethod.getReturnType()));
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

    /**
     * Create a new dynamic invoker instance for the given method.<br>
     * The invoker class must have a method with the right amount of parameters and right return type. A instance parameter is required at the beginning of the parameter list.<br>
     * Super types of the parameter types/return type are also allowed.<br>
     * Only non-static methods can be used.
     *
     * @param invokerClass The invoker interface class
     * @param method       The method to invoke
     * @param <I>          The invoker interface type
     * @return The invoker instance implementation
     */
    public static <I> I makeDynamicInvoker(@Nonnull final Class<I> invokerClass, @Nonnull final Method method) {
        if (Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Dynamic invoker can only be used for non-static methods");
        String newClassName = dash(method.getDeclaringClass()) + "$DynamicMethodInvoker";
        Method invokerMethod = findInvokerMethod(invokerClass, method, true);
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{dash(invokerClass)});

        MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
        mv.visitVarInsn(opcode("ALOAD"), 0);
        mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        pushArgs(mv, invokerMethod.getParameterTypes(), prepend(method.getParameterTypes(), method.getDeclaringClass()));
        if (Modifier.isInterface(method.getDeclaringClass().getModifiers())) {
            mv.visitMethodInsn(opcode("INVOKEINTERFACE"), dash(method.getDeclaringClass()), method.getName(), desc(method), true);
        } else {
            mv.visitMethodInsn(opcode("INVOKEVIRTUAL"), dash(method.getDeclaringClass()), method.getName(), desc(method), false);
        }
        if (!method.getReturnType().equals(invokerMethod.getReturnType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(invokerMethod.getReturnType()));
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
        List<Method> methods = new ArrayList<>();
        for (Method invokerMethod : Methods.getDeclaredMethods(invokerClass)) {
            if (!Modifier.isAbstract(invokerMethod.getModifiers())) continue;
            if (++abstractMethods > 1) throw new IllegalArgumentException("The invoker class must only have one abstract method");
            if (invokerMethod.getParameterCount() != method.getParameterCount() + (requireInstance ? 1 : 0)) continue;
            if (!invokerMethod.getReturnType().isAssignableFrom(method.getReturnType())) continue;

            boolean hasIncompatibleParameter = false;
            Class<?>[] invokerParameterTypes = invokerMethod.getParameterTypes();
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            for (int i = 0; i < invokerParameterTypes.length; i++) {
                Class<?> invokerParameterType = invokerParameterTypes[i];
                Class<?> methodParameterType = (requireInstance && i == 0) ? method.getDeclaringClass() : methodParameterTypes[i - (requireInstance ? 1 : 0)];
                if (invokerParameterType.isAssignableFrom(methodParameterType)) continue;
                hasIncompatibleParameter = true;
                break;
            }
            if (hasIncompatibleParameter) continue;
            methods.add(invokerMethod);
        }
        if (methods.size() != 1) throw new IllegalArgumentException("Could not find a valid invoker method for: " + method);
        return methods.get(0);
    }

    private static void pushArgs(final MethodVisitorAccess mv, final Class<?>[] supplied, final Class<?>[] target) {
        int stack = 1;
        for (int i = 0; i < supplied.length; i++) {
            Class<?> suppliedType = supplied[i];
            Class<?> targetType = target[i];
            mv.visitVarInsn(getLoadOpcode(suppliedType), stack);
            if (!suppliedType.equals(targetType)) mv.visitTypeInsn(opcode("CHECKCAST"), dash(targetType));
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
