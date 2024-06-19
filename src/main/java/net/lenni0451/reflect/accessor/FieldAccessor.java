package net.lenni0451.reflect.accessor;

import lombok.SneakyThrows;
import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * Generate a getter/setter instance for a field.<br>
 * This can be used to get/set private fields without having to resort to reflection.<br>
 * The implementation is generated using the internal ASM.
 */
public class FieldAccessor {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();

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
    @SneakyThrows
    public static <I> I makeSetter(@Nonnull final Class<?> invokerClass, final Object instance, @Nonnull final Field field) {
        String newClassName = slash(field.getDeclaringClass()) + "$FieldSetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getType()}, void.class);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER") | BUILDER.opcode("ACC_FINAL") | BUILDER.opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)}, cb -> {
            //noinspection Convert2MethodRef
            addConstructor(cb, newClassName, () -> instance.getClass(), field);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                if (staticField) {
                    mb.var(BUILDER.opcode(getLoadOpcode(invokerMethod.getParameterTypes()[0])), 1);
                    if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(field.getType()));
                    mb.field(BUILDER.opcode("PUTSTATIC"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                } else {
                    mb.var(BUILDER.opcode("ALOAD"), 0);
                    mb.field(BUILDER.opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
                    mb.var(BUILDER.opcode(getLoadOpcode(invokerMethod.getParameterTypes()[0])), 1);
                    if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(field.getType()));
                    mb.field(BUILDER.opcode("PUTFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                }
                mb.insn(BUILDER.opcode("RETURN"));
                mb.maxs(2, 2);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(field.getDeclaringClass());
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
    @SneakyThrows
    public static <I> I makeDynamicSetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = slash(field.getDeclaringClass()) + "$DynamicFieldSetter";
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass(), field.getType()}, void.class);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER") | BUILDER.opcode("ACC_FINAL") | BUILDER.opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)}, cb -> {
            addConstructor(cb, newClassName, null, field);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                mb.var(BUILDER.opcode("ALOAD"), 1);
                if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mb.type(BUILDER.opcode("CHECKCAST"), slash(field.getDeclaringClass()));
                mb.var(BUILDER.opcode(getLoadOpcode(invokerMethod.getParameterTypes()[1])), 2);
                if (!invokerMethod.getParameterTypes()[1].equals(field.getType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(field.getType()));
                mb.field(BUILDER.opcode("PUTFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                mb.insn(BUILDER.opcode("RETURN"));
                mb.maxs(2, 3);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(field.getDeclaringClass());
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
    @SneakyThrows
    public static <I> I makeGetter(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Field field) {
        String newClassName = slash(field.getDeclaringClass()) + "$FieldGetter";
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[0], field.getType());
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER") | BUILDER.opcode("ACC_FINAL") | BUILDER.opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)}, cb -> {
            //noinspection Convert2MethodRef
            addConstructor(cb, newClassName, () -> instance.getClass(), field);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                if (staticField) {
                    mb.field(BUILDER.opcode("GETSTATIC"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                } else {
                    mb.var(BUILDER.opcode("ALOAD"), 0);
                    mb.field(BUILDER.opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
                    mb.field(BUILDER.opcode("GETFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                }
                if (!field.getType().equals(invokerMethod.getReturnType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
                mb.insn(BUILDER.opcode(getReturnOpcode(invokerMethod.getReturnType())));
                mb.maxs(1, 1);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(field.getDeclaringClass());
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
    @SneakyThrows
    public static <I> I makeDynamicGetter(@Nonnull final Class<I> invokerClass, @Nonnull final Field field) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Dynamic setter can only be used for non-static fields");
        String newClassName = slash(field.getDeclaringClass()) + "$DynamicFieldGetter";
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass()}, field.getType());
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER") | BUILDER.opcode("ACC_FINAL") | BUILDER.opcode("ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)}, cb -> {
            addConstructor(cb, newClassName, null, field);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                mb.var(BUILDER.opcode("ALOAD"), 1);
                if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mb.type(BUILDER.opcode("CHECKCAST"), slash(field.getDeclaringClass()));
                mb.field(BUILDER.opcode("GETFIELD"), slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                if (!invokerMethod.getReturnType().equals(field.getType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
                mb.insn(BUILDER.opcode(getReturnOpcode(invokerMethod.getReturnType())));
                mb.maxs(1, 2);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(field.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (I) Constructors.invoke(constructor);
    }

    private static Method findInvokerMethod(final Class<?> invokerClass, final Class<?>[] parameterTypes, final Class<?> returnType) {
        if (!Modifier.isInterface(invokerClass.getModifiers())) throw new IllegalArgumentException("The invoker class must be an interface");

        int abstractMethods = 0;
        Method matched = null;
        for (Method invokerMethod : Methods.getDeclaredMethods(invokerClass)) {
            if (!Modifier.isAbstract(invokerMethod.getModifiers())) continue;
            if (++abstractMethods > 1) throw new IllegalArgumentException("The invoker class must only have one abstract method");
            if (invokerMethod.getParameterCount() != parameterTypes.length)
                throw new IllegalArgumentException("The invoker method must have " + parameterTypes.length + " parameters");
            if (!invokerMethod.getReturnType().isAssignableFrom(returnType))
                throw new IllegalArgumentException("The invoker method return type must be of type " + returnType.getName());

            Class<?>[] invokerParameterTypes = invokerMethod.getParameterTypes();
            for (int i = 0; i < invokerParameterTypes.length; i++) {
                if (invokerParameterTypes[i].isAssignableFrom(parameterTypes[i])) continue;
                throw new IllegalArgumentException("The invoker method parameter " + i + " must be of type " + parameterTypes[i].getName());
            }
            matched = invokerMethod;
        }
        if (matched == null) throw new IllegalArgumentException("Could not find a valid invoker method for: " + mdesc(returnType, parameterTypes));
        return matched;
    }

    private static void addConstructor(final ClassBuilder cb, final String newClassName, @Nullable final Supplier<Class<?>> instanceType, final Field field) {
        if (Modifier.isStatic(field.getModifiers()) || instanceType == null) {
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", "()V", null, null, mb -> mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .method(BUILDER.opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false)
                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(1, 1)
            );
        } else {
            String instanceTypeDesc = desc(instanceType.get());
            cb.field(BUILDER.opcode("ACC_PRIVATE") | BUILDER.opcode("ACC_FINAL"), "instance", instanceTypeDesc, null, null, fb -> {});

            cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", "(" + instanceTypeDesc + ")V", null, null, mb -> mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .method(BUILDER.opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false)
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .var(BUILDER.opcode("ALOAD"), 1)
                    .field(BUILDER.opcode("PUTFIELD"), newClassName, "instance", instanceTypeDesc)
                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(2, 2)
            );
        }
    }

}
