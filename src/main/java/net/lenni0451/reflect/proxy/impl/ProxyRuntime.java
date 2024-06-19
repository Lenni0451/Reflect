package net.lenni0451.reflect.proxy.impl;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

@ApiStatus.Internal
public class ProxyRuntime {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();

    public static ProxyMethod makeProxyMethod(final Object instance, final Method method) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = slash(instance.getClass()) + "$ProxyMethodImpl";
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_PUBLIC"), className, null, slash(Object.class), new String[]{slash(ProxyMethod.class)}, cb -> {
            cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), "INVOKE_OTHER", desc(MethodHandle.class), null, null);
            cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), "INVOKE_SUPER", desc(MethodHandle.class), null, null);
            cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), "instance", desc(instance.getClass()), null, null);
            cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), "method", desc(Method.class), null, null);
            cb.method(BUILDER.opcode("ACC_PUBLIC", "ACC_STATIC"), "<clinit>", mdesc(void.class), null, null, mb -> {
                mb
                        .typeLdc(BUILDER, method.getDeclaringClass()) //Instance class
                        .ldc(method.getName()) //Method name
                        .intPush(BUILDER, method.getParameterCount())
                        .type(BUILDER.opcode("ANEWARRAY"), slash(Class.class)); //Parameter array
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    Class<?> parameter = method.getParameterTypes()[i];
                    mb
                            .insn(BUILDER.opcode("DUP"))
                            .intPush(BUILDER, i)
                            .typeLdc(BUILDER, parameter)
                            .insn(BUILDER.opcode("AASTORE"));
                }
                mb
                        .typeLdc(BUILDER, method.getReturnType()) //Return type
                        .method(BUILDER.opcode("INVOKESTATIC"), slash(ProxyRuntime.class), "getMethodHandles", mdesc(MethodHandle[].class, Class.class, String.class, Class[].class, Class.class), false);
                mb //Store the method handles
                        .insn(BUILDER.opcode("DUP"))
                        .intPush(BUILDER, 0)
                        .insn(BUILDER.opcode("AALOAD"))
                        .field(BUILDER.opcode("PUTSTATIC"), className, "INVOKE_OTHER", desc(MethodHandle.class))

                        .intPush(BUILDER, 1)
                        .insn(BUILDER.opcode("AALOAD"))
                        .field(BUILDER.opcode("PUTSTATIC"), className, "INVOKE_SUPER", desc(MethodHandle.class));

                mb
                        .insn(BUILDER.opcode("RETURN"))
                        .maxs(6, 0);
            });
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, instance.getClass(), Method.class), null, null, mb -> mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .method(BUILDER.opcode("INVOKESPECIAL"), slash(Object.class), "<init>", mdesc(void.class), false)

                    .var(BUILDER.opcode("ALOAD"), 0)
                    .var(BUILDER.opcode("ALOAD"), 1)
                    .field(BUILDER.opcode("PUTFIELD"), className, "instance", desc(instance.getClass()))

                    .var(BUILDER.opcode("ALOAD"), 0)
                    .var(BUILDER.opcode("ALOAD"), 2)
                    .field(BUILDER.opcode("PUTFIELD"), className, "method", desc(Method.class))

                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(3, 3)
            );
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "getInvokedMethod", mdesc(Method.class), null, null, mb -> mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .field(BUILDER.opcode("GETFIELD"), className, "method", desc(Method.class))
                    .insn(BUILDER.opcode("ARETURN"))
                    .maxs(2, 1)
            );
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeWith", mdesc(Object.class, Object.class, Object[].class), null, null, mb -> {
                String polymorphicSignature = "(" + desc(method.getDeclaringClass());
                for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
                polymorphicSignature += ")" + desc(method.getReturnType());

                mb.field(BUILDER.opcode("GETSTATIC"), className, "INVOKE_OTHER", desc(MethodHandle.class));
                mb //Cast instance to owner class
                        .var(BUILDER.opcode("ALOAD"), 1)
                        .type(BUILDER.opcode("CHECKCAST"), slash(method.getDeclaringClass()));
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    Class<?> parameter = method.getParameterTypes()[i];
                    mb //Load and cast all parameters
                            .var(BUILDER.opcode("ALOAD"), 2)
                            .intPush(BUILDER, i)
                            .insn(BUILDER.opcode("AALOAD"))
                            .type(BUILDER.opcode("CHECKCAST"), slash(boxed(parameter)))
                            .unbox(BUILDER, parameter);
                }
                mb //invokeExact() so the JVM can inline the method call
                        .method(BUILDER.opcode("INVOKEVIRTUAL"), slash(MethodHandle.class), "invokeExact", polymorphicSignature, false);
                if (method.getReturnType().equals(void.class)) mb.insn(BUILDER.opcode("ACONST_NULL"));
                else mb.box(BUILDER, method.getReturnType());
                mb
                        .insn(BUILDER.opcode("ARETURN"))
                        .maxs(method.getParameterCount() + 2, method.getParameterCount() + 1)
                ;
            });
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeSuper", mdesc(Object.class, Object[].class), null, null, mb -> {
                String polymorphicSignature = "(" + desc(method.getDeclaringClass());
                for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
                polymorphicSignature += ")" + desc(method.getReturnType());

                mb.field(BUILDER.opcode("GETSTATIC"), className, "INVOKE_SUPER", desc(MethodHandle.class));
                mb //Cast instance to owner class
                        .var(BUILDER.opcode("ALOAD"), 0)
                        .field(BUILDER.opcode("GETFIELD"), className, "instance", desc(instance.getClass()))
                        .type(BUILDER.opcode("CHECKCAST"), slash(method.getDeclaringClass()));
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    Class<?> parameter = method.getParameterTypes()[i];
                    mb //Load and cast all parameters
                            .var(BUILDER.opcode("ALOAD"), 1)
                            .intPush(BUILDER, i)
                            .insn(BUILDER.opcode("AALOAD"))
                            .type(BUILDER.opcode("CHECKCAST"), slash(boxed(parameter)))
                            .unbox(BUILDER, parameter);
                }
                mb //invokeExact() so the JVM can inline the method call
                        .method(BUILDER.opcode("INVOKEVIRTUAL"), slash(MethodHandle.class), "invokeExact", polymorphicSignature, false);
                if (method.getReturnType().equals(void.class)) mb.insn(BUILDER.opcode("ACONST_NULL"));
                else mb.box(BUILDER, method.getReturnType());
                mb
                        .insn(BUILDER.opcode("ARETURN"))
                        .maxs(method.getParameterCount() + 2, method.getParameterCount() + 1)
                ;
            });
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "cancel", mdesc(Object.class), null, null, mb -> {
                if (method.getReturnType() == void.class) {
                    mb.insn(BUILDER.opcode("ACONST_NULL"));
                } else if (method.getReturnType() == boolean.class) {
                    mb.field(BUILDER.opcode("GETSTATIC"), slash(Boolean.class), "FALSE", desc(Boolean.class));
                } else if (method.getReturnType() == byte.class || method.getReturnType() == short.class || method.getReturnType() == char.class || method.getReturnType() == int.class) {
                    mb
                            .intPush(BUILDER, 0)
                            .box(BUILDER, method.getReturnType());
                } else if (method.getReturnType() == long.class) {
                    mb
                            .ldc(0L)
                            .box(BUILDER, method.getReturnType());
                } else if (method.getReturnType() == float.class) {
                    mb
                            .ldc(0F)
                            .box(BUILDER, method.getReturnType());
                } else if (method.getReturnType() == double.class) {
                    mb
                            .ldc(0D)
                            .box(BUILDER, method.getReturnType());
                } else {
                    mb.insn(BUILDER.opcode("ACONST_NULL"));
                }
                mb.insn(BUILDER.opcode("ARETURN"))
                        .maxs(1, 1);
            });
        });

        Class<?> clazz = builtClass.defineAnonymous(instance.getClass());
        Constructor<?> constructor = clazz.getDeclaredConstructor(instance.getClass(), Method.class);
        constructor.setAccessible(true);
        return (ProxyMethod) constructor.newInstance(instance, method);
    }

    public static MethodHandle[] getMethodHandles(final Class<?> owner, final String name, final Class<?>[] parameters, final Class<?> returnType) throws NoSuchMethodException, IllegalAccessException {
        return new MethodHandle[]{
                TRUSTED_LOOKUP.findVirtual(owner, name, MethodType.methodType(returnType, parameters)),
                TRUSTED_LOOKUP.findSpecial(owner, name, MethodType.methodType(returnType, parameters), owner)
        };
    }

}
