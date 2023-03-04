package net.lenni0451.reflect.accessor;

import net.lenni0451.reflect.ASMAccess;
import net.lenni0451.reflect.Constructors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.lenni0451.reflect.ASMAccess.*;

/**
 * Generate a getter/setter instance for a field.<br>
 * This can be used to get/set private fields without having to resort to reflection.<br>
 * The implementation is generated using the internal ASM.
 */
public class FieldAccessor {

    /**
     * Create a new setter instance for the given field.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param instance The instance of the class the field is in
     * @param field    The field to get
     * @param <T>      The type of the field
     * @return The setter instance
     */
    public static <T> Consumer<T> makeSetter(final Object instance, @Nonnull final Field field) {
        String newClassName = dash(field.getDeclaringClass()) + "$FieldSetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{"java/util/function/Consumer"});

        //noinspection Convert2MethodRef
        addConstructor(acc, newClassName, () -> instance.getClass(), field);

        ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "accept", "(Ljava/lang/Object;)V", null, null);
        if (staticField) {
            mv.visitVarInsn(opcode("ALOAD"), 1);
            if (!Object.class.equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(field.getType()));
            mv.visitFieldInsn(opcode("PUTSTATIC"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        } else {
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitFieldInsn(opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
            mv.visitVarInsn(opcode("ALOAD"), 1);
            if (!Object.class.equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(field.getType()));
            mv.visitFieldInsn(opcode("PUTFIELD"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        }
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        if (staticField) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (Consumer<T>) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (Consumer<T>) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new dynamic setter instance for the given field.<br>
     * Only non-static fields can be used.
     *
     * @param field The field to get
     * @param <O>   The type of the class the field is in
     * @param <T>   The type of the field
     * @return The dynamic setter instance
     */
    public static <O, T> BiConsumer<O, T> makeDynamicSetter(@Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = dash(field.getDeclaringClass()) + "$DynamicFieldSetter";
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{"java/util/function/BiConsumer"});

        addConstructor(acc, newClassName, null, field);

        ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitVarInsn(opcode("ALOAD"), 1);
        if (!Object.class.equals(field.getDeclaringClass())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(field.getDeclaringClass()));
        mv.visitVarInsn(opcode("ALOAD"), 2);
        if (!Object.class.equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(field.getType()));
        mv.visitFieldInsn(opcode("PUTFIELD"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        mv.visitInsn(opcode("RETURN"));
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (BiConsumer<O, T>) Constructors.invoke(constructor);
    }

    /**
     * Create a new getter instance for the given field.<br>
     * The instance parameter is only used if the field is not static.
     *
     * @param instance The instance of the class the field is in
     * @param field    The field to get
     * @param <T>      The type of the field
     * @return The getter instance
     */
    public static <T> Supplier<T> makeGetter(final Object instance, @Nonnull final Field field) {
        String newClassName = dash(field.getDeclaringClass()) + "$FieldGetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{"java/util/function/Supplier"});

        //noinspection Convert2MethodRef
        addConstructor(acc, newClassName, () -> instance.getClass(), field);

        ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "get", "()Ljava/lang/Object;", null, null);
        if (staticField) {
            mv.visitFieldInsn(opcode("GETSTATIC"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        } else {
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitFieldInsn(opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
            mv.visitFieldInsn(opcode("GETFIELD"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        }
        if (!Object.class.equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(Object.class));
        mv.visitInsn(opcode("ARETURN"));
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        if (staticField) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (Supplier<T>) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (Supplier<T>) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new dynamic getter instance for the given field.<br>
     * Only non-static fields can be used.
     *
     * @param field The field to get
     * @param <O>   The type of the class the field is in
     * @param <T>   The type of the field
     * @return The dynamic getter instance
     */
    public static <O, T> Function<O, T> makeDynamicGetter(@Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = dash(field.getDeclaringClass()) + "$DynamicFieldGetter";
        ASMAccess acc = ASMAccess.create(opcode("ACC_SUPER") | opcode("ACC_FINAL") | opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{"java/util/function/Function"});

        addConstructor(acc, newClassName, null, field);

        ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitVarInsn(opcode("ALOAD"), 1);
        if (!Object.class.equals(field.getDeclaringClass())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(field.getDeclaringClass()));
        mv.visitFieldInsn(opcode("GETFIELD"), dash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
        if (!Object.class.equals(field.getType())) mv.visitTypeInsn(opcode("CHECKCAST"), dash(Object.class));
        mv.visitInsn(opcode("ARETURN"));
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        Class<?> clazz = acc.defineMetafactory(field.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (Function<O, T>) Constructors.invoke(constructor);
    }

    private static void addConstructor(final ASMAccess acc, final String newClassName, @Nullable final Supplier<Class<?>> instanceType, final Field field) {
        if (Modifier.isStatic(field.getModifiers()) || instanceType == null) {
            ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "()V", null, null);
            mv.visitVarInsn(opcode("ALOAD"), 0);
            mv.visitMethodInsn(opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(opcode("RETURN"));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        } else {
            String instanceTypeDesc = desc(instanceType.get());
            acc.visitField(opcode("ACC_PRIVATE") | opcode("ACC_FINAL"), "instance", instanceTypeDesc, null, null);

            ASMAccess.MethodVisitorAccess mv = acc.visitMethod(opcode("ACC_PUBLIC"), "<init>", "(" + instanceTypeDesc + ")V", null, null);
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
