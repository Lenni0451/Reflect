package net.lenni0451.reflect.accessor;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.wrapper.ASMWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.lenni0451.reflect.wrapper.ASMWrapper.*;

/**
 * Generate a getter/setter instance for a field.<br>
 * This can be used to get/set private fields without having to resort to reflection.<br>
 * The implementation is generated using the internal ASM.
 */
public class FieldAccessor {

    /**
     * Create a new setter instance for the given field.<br>
     * The invoker class must have a method with one parameter and no return type.<br>
     * Super types of the parameter type are also allowed.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the field is in
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The setter instance
     */
    public static <I> I makeSetter(@Nonnull final Class<?> invokerClass, final Object instance, @Nonnull final Field field) {
        String newClassName = slash(field.getDeclaringClass()) + "$FieldSetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getType()}, void.class);
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        //noinspection Convert2MethodRef
        addConstructor(acc, newClassName, () -> instance.getClass(), field);

        ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        if (staticField) {
            mv.visitVarInsn(getLoadOpcode(invokerMethod.getParameterTypes()[0]), 1);
            if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(field.getType()));
            mv.visitFieldInsn(opcode("PUTSTATIC"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        } else {
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitFieldInsn(opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
            mv.visitVarInsn(getLoadOpcode(invokerMethod.getParameterTypes()[0]), 1);
            if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(field.getType()));
            mv.visitFieldInsn(opcode("PUTFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        }
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        if (staticField) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (I) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (I) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new dynamic setter instance for the given field.<br>
     * The invoker class must have a method with two parameters and no return type.<br>
     * Super types of the parameter types are also allowed.<br>
     * Only non-static fields can be used.
     *
     * @param invokerClass The invoker interface class
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The dynamic setter instance
     */
    public static <I> I makeDynamicSetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = slash(field.getDeclaringClass()) + "$DynamicFieldSetter";
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass(), field.getType()}, void.class);
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        addConstructor(acc, newClassName, null, field);

        ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        mv.visitVarInsn(opcode("ALOAD"), 1);
        if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(field.getDeclaringClass()));
        mv.visitVarInsn(getLoadOpcode(invokerMethod.getParameterTypes()[1]), 2);
        if (!invokerMethod.getParameterTypes()[1].equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(field.getType()));
        mv.visitFieldInsn(opcode("PUTFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (I) Constructors.invoke(constructor);
    }

    /**
     * Create a new getter instance for the given field.<br>
     * The invoker class must have a method with no parameters and right return type.<br>
     * Super types of the return type are also allowed.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param invokerClass The invoker interface class
     * @param instance     The instance of the class the field is in
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The getter instance
     */
    public static <I> I makeGetter(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Field field) {
        String newClassName = slash(field.getDeclaringClass()) + "$FieldGetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[0], field.getType());
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        //noinspection Convert2MethodRef
        addConstructor(acc, newClassName, () -> instance.getClass(), field);

        ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        if (staticField) {
            mv.visitFieldInsn(opcode("GETSTATIC"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        } else {
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitFieldInsn(opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
            mv.visitFieldInsn(opcode("GETFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        }
        if (!field.getType().equals(invokerMethod.getReturnType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
        mv.visitInsn(getReturnOpcode(invokerMethod.getReturnType()));
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        if (staticField) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (I) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (I) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new dynamic getter instance for the given field.<br>
     * The invoker class must have a method with one parameter and right return type.<br>
     * Super types of the parameter type and return type are also allowed.<br>
     * Only non-static fields can be used.
     *
     * @param invokerClass The invoker interface class
     * @param field        The field to get
     * @param <I>          The invoker interface type
     * @return The dynamic getter instance
     */
    public static <I> I makeDynamicGetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = slash(field.getDeclaringClass()) + "$DynamicFieldGetter";
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass()}, field.getType());
        ASMWrapper acc = ASMWrapper.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)});

        addConstructor(acc, newClassName, null, field);

        ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null);
        mv.visitVarInsn(opcode("ALOAD"), 1);
        if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(field.getDeclaringClass()));
        mv.visitFieldInsn(opcode("GETFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        if (!invokerMethod.getReturnType().equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
        mv.visitInsn(getReturnOpcode(invokerMethod.getReturnType()));
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (I) Constructors.invoke(constructor);
    }

    private static Method findInvokerMethod(final Class<?> invokerClass, final Class<?>[] parameterTypes, final Class<?> returnType) {
        if (!Modifier.isInterface(invokerClass.getModifiers())) throw new IllegalArgumentException("The invoker class must be an interface");

        int abstractMethods = 0;
        List<Method> methods = new ArrayList<>();
        for (Method invokerMethod : Methods.getDeclaredMethods(invokerClass)) {
            if (!Modifier.isAbstract(invokerMethod.getModifiers())) continue;
            if (++abstractMethods > 1) throw new IllegalArgumentException("The invoker class must only have one abstract method");
            if (invokerMethod.getParameterCount() != parameterTypes.length) continue;
            if (!invokerMethod.getReturnType().isAssignableFrom(returnType)) continue;

            boolean hasIncompatibleParameter = false;
            Class<?>[] invokerParameterTypes = invokerMethod.getParameterTypes();
            for (int i = 0; i < invokerParameterTypes.length; i++) {
                if (invokerParameterTypes[i].isAssignableFrom(parameterTypes[i])) continue;
                hasIncompatibleParameter = true;
                break;
            }
            if (hasIncompatibleParameter) continue;
            methods.add(invokerMethod);
        }
        if (methods.size() != 1) throw new IllegalArgumentException("Could not find a valid invoker method for: " + desc(parameterTypes, returnType));
        return methods.get(0);
    }

    private static void addConstructor(final ASMWrapper acc, final String newClassName, @Nullable final Supplier<Class<?>> instanceType, final Field field) {
        if (Modifier.isStatic(field.getModifiers()) || instanceType == null) {
            ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(opcode("RETURN"));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        } else {
            String instanceTypeDesc = desc(instanceType.get());
            acc.visitField(opcode("ACC_PRIVATE") | opcode("ACC_FINAL"), "instance", instanceTypeDesc, null, null);

            ASMWrapper.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "(" + instanceTypeDesc + ")V", null, null);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitVarInsn(opcode("ALOAD"), 1);
            mv.visitFieldInsn(opcode("PUTFIELD"), newClassName, "instance", instanceTypeDesc);
            mv.visitInsn(opcode("RETURN"));
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
    }

}
