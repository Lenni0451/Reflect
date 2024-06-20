package net.lenni0451.reflect.accessor;

import lombok.SneakyThrows;
import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.MethodBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.lenni0451.reflect.accessor.AccessorUtils.addConstructor;
import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * Generate an invoker interface instance for a method.<br>
 * This can be used to call private methods without having to resort to reflection.<br>
 * The implementation is generated at runtime using the internal ASM.
 */
public class MethodAccessor {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();

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
    @SneakyThrows
    public static <I> I makeInvoker(@Nonnull final Class<I> invokerClass, final Object instance, @Nonnull final Method method) {
        String newClassName = slash(method.getDeclaringClass()) + "$MethodInvoker";
        boolean staticMethod = Modifier.isStatic(method.getModifiers());
        Method invokerMethod = findInvokerMethod(invokerClass, method, false);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, slash(Object.class), new String[]{slash(invokerClass)}, cb -> {
            //Disable the inspection because the instance parameter can be null. Just invoking getClass() here would throw an exception
            //noinspection Convert2MethodRef
            addConstructor(BUILDER, cb, () -> instance.getClass(), staticMethod);

            String methodClass = slash(method.getDeclaringClass());
            String methodDesc = desc(method);
            boolean interfaceMethod = Modifier.isInterface(method.getDeclaringClass().getModifiers());
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                if (staticMethod) {
                    pushArgs(mb, invokerMethod.getParameterTypes(), method.getParameterTypes());
                    mb.method(BUILDER.opcode("INVOKESTATIC"), methodClass, method.getName(), methodDesc, interfaceMethod);
                } else {
                    mb.var(BUILDER.opcode("ALOAD"), 0);
                    mb.field(BUILDER.opcode("GETFIELD"), newClassName, "instance", desc(instance.getClass()));
                    pushArgs(mb, invokerMethod.getParameterTypes(), method.getParameterTypes());
                    if (interfaceMethod) {
                        mb.method(BUILDER.opcode("INVOKEINTERFACE"), methodClass, method.getName(), methodDesc, true);
                    } else {
                        mb.method(BUILDER.opcode("INVOKEVIRTUAL"), methodClass, method.getName(), methodDesc, false);
                    }
                }
                if (!method.getReturnType().equals(invokerMethod.getReturnType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
                mb.insn(BUILDER.opcode(getReturnOpcode(invokerMethod.getReturnType())));
                mb.maxs(invokerMethod.getParameterCount() + 1, invokerMethod.getParameterCount() + 1);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(method.getDeclaringClass());
        if (staticMethod) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (I) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (I) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new array invoker instance for the given method.<br>
     * The method parameters are passed as an array. Make sure the types are correct (e.g. Double instead of double) and the order is correct.<br>
     * The instance parameter is only used if the method is not static.
     *
     * @param instance The instance of the class the method is in
     * @param method   The method to invoke
     * @param <R>      The return type
     * @return The invoker instance implementation
     */
    public static <R> Function<Object[], R> makeArrayInvoker(final Object instance, @Nonnull final Method method) {
        boolean staticMethod = Modifier.isStatic(method.getModifiers());
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), slash(method.getDeclaringClass()) + "$ArrayMethodInvoker", null, slash(Object.class), new String[]{slash(Function.class)}, cb -> {
            //Disable the inspection because the instance parameter can be null. Just invoking getClass() here would throw an exception
            //noinspection Convert2MethodRef
            addConstructor(BUILDER, cb, () -> instance.getClass(), staticMethod);

            String methodClass = slash(method.getDeclaringClass());
            String methodDesc = desc(method);
            boolean interfaceMethod = Modifier.isInterface(method.getDeclaringClass().getModifiers());
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "apply", mdesc(Object.class, Object.class), null, null, mb -> {
                if (!staticMethod) {
                    mb
                            .var(BUILDER.opcode("ALOAD"), 0)
                            .field(BUILDER.opcode("GETFIELD"), cb.getName(), "instance", desc(instance.getClass()));
                }
                pushArrayArgs(mb, method, 1);
                if (staticMethod) {
                    mb.method(BUILDER.opcode("INVOKESTATIC"), methodClass, method.getName(), methodDesc, interfaceMethod);
                } else {
                    if (interfaceMethod) {
                        mb.method(BUILDER.opcode("INVOKEINTERFACE"), methodClass, method.getName(), methodDesc, true);
                    } else {
                        mb.method(BUILDER.opcode("INVOKEVIRTUAL"), methodClass, method.getName(), methodDesc, false);
                    }
                }
                mb
                        .box(BUILDER, method.getReturnType())
                        .insn(BUILDER.opcode("ARETURN"))
                        .maxs(method.getParameterCount() + 2, 2);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(method.getDeclaringClass());
        if (staticMethod) {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
            return (Function<Object[], R>) Constructors.invoke(constructor);
        } else {
            Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz, instance.getClass());
            return (Function<Object[], R>) Constructors.invoke(constructor, instance);
        }
    }

    /**
     * Create a new dynamic invoker instance for the given method.<br>
     * The invoker class must have a method with the right amount of parameters and right return type. An instance parameter is required at the beginning of the parameter list.<br>
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
        String newClassName = slash(method.getDeclaringClass()) + "$DynamicMethodInvoker";
        Method invokerMethod = findInvokerMethod(invokerClass, method, true);
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), newClassName, null, slash(Object.class), new String[]{slash(invokerClass)}, cb -> {
            addConstructor(BUILDER, cb, null, false);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), invokerMethod.getName(), desc(invokerMethod), null, null, mb -> {
                pushArgs(mb, invokerMethod.getParameterTypes(), prepend(method.getParameterTypes(), method.getDeclaringClass()));
                if (Modifier.isInterface(method.getDeclaringClass().getModifiers())) {
                    mb.method(BUILDER.opcode("INVOKEINTERFACE"), slash(method.getDeclaringClass()), method.getName(), desc(method), true);
                } else {
                    mb.method(BUILDER.opcode("INVOKEVIRTUAL"), slash(method.getDeclaringClass()), method.getName(), desc(method), false);
                }
                if (!method.getReturnType().equals(invokerMethod.getReturnType())) mb.type(BUILDER.opcode("CHECKCAST"), slash(invokerMethod.getReturnType()));
                mb.insn(BUILDER.opcode(getReturnOpcode(invokerMethod.getReturnType())));
                mb.maxs(invokerMethod.getParameterCount() + 1, invokerMethod.getParameterCount() + 1);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(method.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (I) Constructors.invoke(constructor);
    }

    /**
     * Create a new dynamic array invoker instance for the given method.<br>
     * The first parameter is the instance of the class the method is in.<br>
     * The second parameter is an array of the method parameters. Make sure the types are correct (e.g. Double instead of double) and the order is correct.<br>
     * Only non-static methods can be used.
     *
     * @param method The method to invoke
     * @param <I>    The instance type
     * @param <R>    The return type
     * @return The invoker instance implementation
     */
    public static <I, R> BiFunction<I, Object[], R> makeDynamicArrayInvoker(@Nonnull final Method method) {
        if (Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Dynamic invoker can only be used for non-static methods");
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_SUPER", "ACC_FINAL", "ACC_SYNTHETIC"), slash(method.getDeclaringClass()) + "$DynamicArrayMethodInvoker", null, slash(Object.class), new String[]{slash(BiFunction.class)}, cb -> {
            addConstructor(BUILDER, cb, null, false);
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "apply", mdesc(Object.class, Object.class, Object.class), null, null, mb -> {
                mb
                        .var(BUILDER.opcode("ALOAD"), 1)
                        .type(BUILDER.opcode("CHECKCAST"), slash(method.getDeclaringClass()));
                pushArrayArgs(mb, method, 2);
                if (Modifier.isInterface(method.getDeclaringClass().getModifiers())) {
                    mb.method(BUILDER.opcode("INVOKEINTERFACE"), slash(method.getDeclaringClass()), method.getName(), desc(method), true);
                } else {
                    mb.method(BUILDER.opcode("INVOKEVIRTUAL"), slash(method.getDeclaringClass()), method.getName(), desc(method), false);
                }
                mb
                        .box(BUILDER, method.getReturnType())
                        .insn(BUILDER.opcode("ARETURN"))
                        .maxs(method.getParameterCount() + 2, 3);
            });
        });

        Class<?> clazz = builtClass.defineMetafactory(method.getDeclaringClass());
        Constructor<?> constructor = Constructors.getDeclaredConstructor(clazz);
        return (BiFunction<I, Object[], R>) Constructors.invoke(constructor);
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

    private static void pushArgs(final MethodBuilder mb, final Class<?>[] supplied, final Class<?>[] target) {
        int stack = 1;
        for (int i = 0; i < supplied.length; i++) {
            Class<?> suppliedType = supplied[i];
            Class<?> targetType = target[i];
            mb.var(BUILDER.opcode(getLoadOpcode(suppliedType)), stack);
            if (!suppliedType.equals(targetType)) mb.type(BUILDER.opcode("CHECKCAST"), slash(targetType));
            stack += getStackSize(suppliedType);
        }
    }

    private static void pushArrayArgs(final MethodBuilder mb, final Method method, final int arrayIndex) {
        mb
                .var(BUILDER.opcode("ALOAD"), arrayIndex)
                .type(BUILDER.opcode("CHECKCAST"), desc(Object[].class))
                .var(BUILDER.opcode("ASTORE"), arrayIndex);
        for (int i = 0; i < method.getParameterCount(); i++) {
            Class<?> parameter = method.getParameterTypes()[i];
            mb
                    .var(BUILDER.opcode("ALOAD"), arrayIndex)
                    .intPush(BUILDER, i)
                    .insn(BUILDER.opcode("AALOAD"))
                    .type(BUILDER.opcode("CHECKCAST"), slash(boxed(parameter)))
                    .unbox(BUILDER, parameter);
        }
    }

    private static Class<?>[] prepend(final Class<?>[] classes, final Class<?> other) {
        Class<?>[] newClasses = new Class<?>[classes.length + 1];
        newClasses[0] = other;
        System.arraycopy(classes, 0, newClasses, 1, classes.length);
        return newClasses;
    }

}
