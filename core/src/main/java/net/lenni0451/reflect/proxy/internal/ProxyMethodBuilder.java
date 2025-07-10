package net.lenni0451.reflect.proxy.internal;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.proxy.impl.ProxyMethod;
import net.lenni0451.reflect.proxy.impl.ProxyRuntime;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * A builder for creating the {@link ProxyMethod} implementations.<br>
 * They are unique to every method and therefore need to be created at runtime.
 */
@ApiStatus.Internal
public class ProxyMethodBuilder {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();

    public static Class<ProxyMethod> buildProxyMethodClass(final Class<?> proxyClass, final Method method, @Nullable final Method originalMethod) {
        BuiltClass builtClass = BUILDER.class_(BUILDER.opcode("ACC_PUBLIC"), slash(proxyClass) + "$ProxyMethodImpl", null, slash(Object.class), new String[]{slash(ProxyMethod.class)}, cb -> {
            addFields(proxyClass, cb);
            addStaticBlock(method, originalMethod, cb);
            addConstructor(proxyClass, cb);
            addMethodGetter(cb);
            addInvokeWith(method, cb);
            addInvokeSuper(proxyClass, originalMethod == null ? method : originalMethod, cb);
            addCancel(method, cb);
        });

        return (Class<ProxyMethod>) builtClass.defineAnonymous(proxyClass);
    }

    private static void addFields(final Class<?> proxyClass, final ClassBuilder cb) {
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), "INVOKE_OTHER", desc(MethodHandle.class), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), "INVOKE_SUPER", desc(MethodHandle.class), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), "instance", desc(proxyClass), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), "method", desc(Method.class), null, null);
    }

    private static void addStaticBlock(final Method method, @Nullable final Method originalMethod, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC", "ACC_STATIC"), "<clinit>", mdesc(void.class), null, null, mb -> {
            mb.typeLdc(BUILDER, method.getDeclaringClass()); //Instance class
            if (originalMethod != null) {
                mb.typeLdc(BUILDER, originalMethod.getDeclaringClass()); //Super class
            } else {
                mb.dup();
            }
            mb
                    .ldc(method.getName()) //Method name
                    .intPush(method.getParameterCount())
                    .anewarray(slash(Class.class)); //Parameter array
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> parameter = method.getParameterTypes()[i];
                mb
                        .dup()
                        .intPush(i)
                        .typeLdc(BUILDER, parameter)
                        .aastore();
            }
            mb
                    .typeLdc(BUILDER, method.getReturnType()) //Return type
                    .invokestatic(slash(ProxyRuntime.class), "getMethodHandles", mdesc(MethodHandle[].class, Class.class, Class.class, String.class, Class[].class, Class.class), false);
            mb //Store the method handles
                    .dup()
                    .intPush(0)
                    .aaload()
                    .putstatic(cb.getName(), "INVOKE_OTHER", desc(MethodHandle.class))

                    .intPush(1)
                    .aaload()
                    .putstatic(cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class));

            mb
                    .return_()
                    .maxs(6, 0);
        });
    }

    private static void addConstructor(final Class<?> proxyClass, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, proxyClass, Method.class), null, null, mb -> mb
                .aload(0)
                .invokespecial(slash(Object.class), "<init>", mdesc(void.class), false)

                .aload(0)
                .aload(1)
                .putfield(cb.getName(), "instance", desc(proxyClass))

                .aload(0)
                .aload(2)
                .putfield(cb.getName(), "method", desc(Method.class))

                .return_()
                .maxs(3, 3)
        );
    }

    private static void addMethodGetter(final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "getInvokedMethod", mdesc(Method.class), null, null, mb -> mb
                .aload(0)
                .getfield(cb.getName(), "method", desc(Method.class))
                .areturn()
                .maxs(2, 1)
        );
    }

    private static void addInvokeWith(final Method method, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeWith", mdesc(Object.class, Object.class, Object[].class), null, null, mb -> {
            String polymorphicSignature = "(" + desc(method.getDeclaringClass());
            for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
            polymorphicSignature += ")" + desc(method.getReturnType());

            mb.getstatic(cb.getName(), "INVOKE_OTHER", desc(MethodHandle.class));
            mb //Cast instance to owner class
                    .aload(1)
                    .checkcast(slash(method.getDeclaringClass()));
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> parameter = method.getParameterTypes()[i];
                mb //Load and cast all parameters
                        .aload(2)
                        .intPush(i)
                        .aaload()
                        .checkcast(slash(boxed(parameter)))
                        .unbox(BUILDER, parameter);
            }
            mb //invokeExact() so the JVM can inline the method call
                    .invokevirtual(slash(MethodHandle.class), "invokeExact", polymorphicSignature, false);
            if (method.getReturnType().equals(void.class)) mb.aconstNull();
            else mb.box(BUILDER, method.getReturnType());
            mb
                    .areturn()
                    .maxs(method.getParameterCount() + 2, method.getParameterCount() + 1)
            ;
        });
    }

    private static void addInvokeSuper(final Class<?> proxyClass, final Method method, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeSuper", mdesc(Object.class, Object[].class), null, null, mb -> {
            String polymorphicSignature = "(" + desc(method.getDeclaringClass());
            for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
            polymorphicSignature += ")" + desc(method.getReturnType());

            BytecodeLabel elseLabel = BUILDER.label();
            mb
                    .aload(0)
                    .getstatic(cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class))
                    .ifnonnull(elseLabel)
                    .new_(slash(AbstractMethodError.class))
                    .dup()
                    .ldc(slash(method.getDeclaringClass()) + "." + method.getName() + desc(method))
                    .invokespecial(slash(AbstractMethodError.class), "<init>", mdesc(void.class, String.class), false)
                    .athrow()
                    .label(elseLabel);

            mb.getstatic(cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class));
            mb //Cast instance to owner class
                    .aload(0)
                    .getfield(cb.getName(), "instance", desc(proxyClass))
                    .checkcast(slash(method.getDeclaringClass()));
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> parameter = method.getParameterTypes()[i];
                mb //Load and cast all parameters
                        .aload(1)
                        .intPush(i)
                        .aaload()
                        .checkcast(slash(boxed(parameter)))
                        .unbox(BUILDER, parameter);
            }
            mb //invokeExact() so the JVM can inline the method call
                    .invokevirtual(slash(MethodHandle.class), "invokeExact", polymorphicSignature, false);
            if (method.getReturnType().equals(void.class)) mb.aconstNull();
            else mb.box(BUILDER, method.getReturnType());
            mb
                    .areturn()
                    .maxs(method.getParameterCount() + 2, method.getParameterCount() + 1)
            ;
        });
    }

    private static void addCancel(final Method method, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "cancel", mdesc(Object.class), null, null, mb -> {
            if (method.getReturnType() == void.class) {
                mb.aconstNull();
            } else if (method.getReturnType() == boolean.class) {
                mb.getstatic(slash(Boolean.class), "FALSE", desc(Boolean.class));
            } else if (method.getReturnType() == byte.class || method.getReturnType() == short.class || method.getReturnType() == char.class || method.getReturnType() == int.class) {
                mb
                        .intPush(0)
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
                mb.aconstNull();
            }
            mb.areturn()
                    .maxs(1, 1);
        });
    }

}
