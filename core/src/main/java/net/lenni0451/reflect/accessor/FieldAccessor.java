package net.lenni0451.reflect.accessor;

import lombok.SneakyThrows;
import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.lenni0451.reflect.accessor.AccessorUtils.addConstructor;
import static net.lenni0451.reflect.accessor.AccessorUtils.makeAccessorName;
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
        String newClassName = makeAccessorName("FieldSetter", field.getDeclaringClass(), field.getName());
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getType()}, void.class);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, slash(Object.class), new String[]{slash(invokerClass)}, cb -> {
            //Disable the inspection because the instance parameter can be null. Just invoking getClass() here would throw an exception
            //noinspection Convert2MethodRef
            addConstructor(BUILDER, cb, () -> instance.getClass(), Modifier.isStatic(field.getModifiers()));
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                if (staticField) {
                    mb.load(invokerMethod.getParameterTypes()[0], 1);
                    if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mb.checkcast(slash(field.getType()));
                    mb.putstatic(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                } else {
                    mb.aload(0);
                    mb.getfield(newClassName, "instance", desc(instance.getClass()));
                    mb.load(invokerMethod.getParameterTypes()[0], 1);
                    if (!invokerMethod.getParameterTypes()[0].equals(field.getType())) mb.checkcast(slash(field.getType()));
                    mb.putfield(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                }
                mb.return_();
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
        String newClassName = makeAccessorName("DynamicFieldSetter", field.getDeclaringClass(), field.getName());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass(), field.getType()}, void.class);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, slash(Object.class), new String[]{slash(invokerClass)}, cb -> {
            addConstructor(BUILDER, cb, null, Modifier.isStatic(field.getModifiers()));
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                mb.aload(1);
                if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mb.checkcast(slash(field.getDeclaringClass()));
                mb.load(invokerMethod.getParameterTypes()[1], 2);
                if (!invokerMethod.getParameterTypes()[1].equals(field.getType())) mb.checkcast(slash(field.getType()));
                mb.putfield(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                mb.return_();
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
        String newClassName = makeAccessorName("FieldGetter", field.getDeclaringClass(), field.getName());
        boolean staticField = Modifier.isStatic(field.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[0], field.getType());
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, slash(Object.class), new String[]{slash(invokerClass)}, cb -> {
            //Disable the inspection because the instance parameter can be null. Just invoking getClass() here would throw an exception
            //noinspection Convert2MethodRef
            addConstructor(BUILDER, cb, () -> instance.getClass(), Modifier.isStatic(field.getModifiers()));
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                if (staticField) {
                    mb.getstatic(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                } else {
                    mb.aload(0);
                    mb.getfield(newClassName, "instance", desc(instance.getClass()));
                    mb.getfield(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                }
                if (!field.getType().equals(invokerMethod.getReturnType())) mb.checkcast(slash(invokerMethod.getReturnType()));
                mb.return_(invokerMethod.getReturnType());
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
        String newClassName = makeAccessorName("DynamicFieldGetter", field.getDeclaringClass(), field.getName());
        Method invokerMethod = findInvokerMethod(invokerClass, new Class[]{field.getDeclaringClass()}, field.getType());
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, "java/lang/Object", new String[]{slash(invokerClass)}, cb -> {
            addConstructor(BUILDER, cb, null, Modifier.isStatic(field.getModifiers()));
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                mb.aload(1);
                if (!invokerMethod.getParameterTypes()[0].equals(field.getDeclaringClass())) mb.checkcast(slash(field.getDeclaringClass()));
                mb.getfield(slash(field.getDeclaringClass()), field.getName(), desc(field.getType()));
                if (!invokerMethod.getReturnType().equals(field.getType())) mb.checkcast(slash(invokerMethod.getReturnType()));
                mb.return_(invokerMethod.getReturnType());
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

}
