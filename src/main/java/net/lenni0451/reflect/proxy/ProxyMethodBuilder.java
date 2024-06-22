package net.lenni0451.reflect.proxy;

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
class ProxyMethodBuilder {

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
                mb.insn(BUILDER.opcode("DUP"));
            }
            mb
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
                    .method(BUILDER.opcode("INVOKESTATIC"), slash(ProxyRuntime.class), "getMethodHandles", mdesc(MethodHandle[].class, Class.class, Class.class, String.class, Class[].class, Class.class), false);
            mb //Store the method handles
                    .insn(BUILDER.opcode("DUP"))
                    .intPush(BUILDER, 0)
                    .insn(BUILDER.opcode("AALOAD"))
                    .field(BUILDER.opcode("PUTSTATIC"), cb.getName(), "INVOKE_OTHER", desc(MethodHandle.class))

                    .intPush(BUILDER, 1)
                    .insn(BUILDER.opcode("AALOAD"))
                    .field(BUILDER.opcode("PUTSTATIC"), cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class));

            mb
                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(6, 0);
        });
    }

    private static void addConstructor(final Class<?> proxyClass, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, proxyClass, Method.class), null, null, mb -> mb
                .var(BUILDER.opcode("ALOAD"), 0)
                .method(BUILDER.opcode("INVOKESPECIAL"), slash(Object.class), "<init>", mdesc(void.class), false)

                .var(BUILDER.opcode("ALOAD"), 0)
                .var(BUILDER.opcode("ALOAD"), 1)
                .field(BUILDER.opcode("PUTFIELD"), cb.getName(), "instance", desc(proxyClass))

                .var(BUILDER.opcode("ALOAD"), 0)
                .var(BUILDER.opcode("ALOAD"), 2)
                .field(BUILDER.opcode("PUTFIELD"), cb.getName(), "method", desc(Method.class))

                .insn(BUILDER.opcode("RETURN"))
                .maxs(3, 3)
        );
    }

    private static void addMethodGetter(final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "getInvokedMethod", mdesc(Method.class), null, null, mb -> mb
                .var(BUILDER.opcode("ALOAD"), 0)
                .field(BUILDER.opcode("GETFIELD"), cb.getName(), "method", desc(Method.class))
                .insn(BUILDER.opcode("ARETURN"))
                .maxs(2, 1)
        );
    }

    private static void addInvokeWith(final Method method, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeWith", mdesc(Object.class, Object.class, Object[].class), null, null, mb -> {
            String polymorphicSignature = "(" + desc(method.getDeclaringClass());
            for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
            polymorphicSignature += ")" + desc(method.getReturnType());

            mb.field(BUILDER.opcode("GETSTATIC"), cb.getName(), "INVOKE_OTHER", desc(MethodHandle.class));
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
    }

    private static void addInvokeSuper(final Class<?> proxyClass, final Method method, final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "invokeSuper", mdesc(Object.class, Object[].class), null, null, mb -> {
            String polymorphicSignature = "(" + desc(method.getDeclaringClass());
            for (Class<?> parameter : method.getParameterTypes()) polymorphicSignature += desc(parameter);
            polymorphicSignature += ")" + desc(method.getReturnType());

            BytecodeLabel elseLabel = BUILDER.label();
            mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .field(BUILDER.opcode("GETSTATIC"), cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class))
                    .jump(BUILDER.opcode("IFNONNULL"), elseLabel)
                    .type(BUILDER.opcode("NEW"), slash(AbstractMethodError.class))
                    .insn(BUILDER.opcode("DUP"))
                    .ldc(slash(method.getDeclaringClass()) + "." + method.getName() + desc(method))
                    .method(BUILDER.opcode("INVOKESPECIAL"), slash(AbstractMethodError.class), "<init>", mdesc(void.class, String.class), false)
                    .insn(BUILDER.opcode("ATHROW"))
                    .label(elseLabel);

            mb.field(BUILDER.opcode("GETSTATIC"), cb.getName(), "INVOKE_SUPER", desc(MethodHandle.class));
            mb //Cast instance to owner class
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .field(BUILDER.opcode("GETFIELD"), cb.getName(), "instance", desc(proxyClass))
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
    }

    private static void addCancel(final Method method, final ClassBuilder cb) {
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
    }

}
