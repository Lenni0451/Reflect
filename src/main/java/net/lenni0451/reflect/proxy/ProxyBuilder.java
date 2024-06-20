package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Fields;
import net.lenni0451.reflect.bytecode.BytecodeUtils;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.proxy.define.ProxyClassDefiner;
import net.lenni0451.reflect.proxy.impl.Proxy;
import net.lenni0451.reflect.proxy.impl.ProxyMethod;
import net.lenni0451.reflect.proxy.impl.ProxyRuntime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * A builder to create proxy classes.
 */
public class ProxyBuilder {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();

    @Nullable
    private Class<?> superClass;
    @Nullable
    private Class<?>[] interfaces;
    private InvocationHandler invocationHandler = InvocationHandler.forwarding();
    private ProxyClassDefiner classDefiner = ProxyClassDefiner.loader(ProxyBuilder.class.getClassLoader());

    private Class<?> proxyClass;

    /**
     * Set the super class of the proxy class.<br>
     * The super class must be public and not final.
     *
     * @param superClass The super class
     * @return This builder
     */
    public ProxyBuilder setSuperClass(@Nullable final Class<?> superClass) {
        if (superClass != null) ProxyUtils.verifySuperClass(superClass);

        this.reset();
        this.superClass = superClass;
        return this;
    }

    /**
     * @return The super class of the proxy class
     */
    @Nullable
    public Class<?> getSuperClass() {
        return this.superClass;
    }

    /**
     * Add an interface to the proxy class.<br>
     * The interface must be public.
     *
     * @param clazz The interface to add
     * @return This builder
     */
    public ProxyBuilder addInterface(@Nonnull final Class<?> clazz) {
        ProxyUtils.verifyInterface(clazz);

        this.reset();
        if (this.interfaces == null) {
            this.interfaces = new Class<?>[]{clazz};
        } else {
            this.interfaces = Arrays.copyOf(this.interfaces, this.interfaces.length + 1);
            this.interfaces[this.interfaces.length - 1] = clazz;
        }
        return this;
    }

    /**
     * Set the interfaces of the proxy class.<br>
     * The interfaces must be public.
     *
     * @param interfaces The interfaces
     * @return This builder
     */
    public ProxyBuilder setInterfaces(@Nullable final Class<?>... interfaces) {
        if (interfaces != null) {
            for (Class<?> inter : interfaces) ProxyUtils.verifyInterface(inter);
        }

        this.reset();
        this.interfaces = interfaces;
        return this;
    }

    /**
     * @return The interfaces of the proxy class
     */
    @Nullable
    public Class<?>[] getInterfaces() {
        return this.interfaces;
    }

    /**
     * Set the invocation handler for the proxy class.
     *
     * @param invocationHandler The invocation handler
     * @return This builder
     */
    public ProxyBuilder setInvocationHandler(@Nonnull final InvocationHandler invocationHandler) {
        this.reset();
        this.invocationHandler = invocationHandler;
        return this;
    }

    /**
     * @return The invocation handler
     */
    public InvocationHandler getInvocationHandler() {
        return this.invocationHandler;
    }

    /**
     * Set the class definer for the proxy class.
     *
     * @param classDefiner The class definer
     * @return This builder
     */
    public ProxyBuilder setClassDefiner(@Nonnull final ProxyClassDefiner classDefiner) {
        this.reset();
        this.classDefiner = classDefiner;
        return this;
    }

    /**
     * @return The current class definer
     */
    public ProxyClassDefiner getClassDefiner() {
        return this.classDefiner;
    }

    /**
     * Build the proxy class.
     *
     * @return The built proxy class
     */
    public ProxyClass build() {
        if (this.proxyClass == null) {
            Reference<Method[]> methodsReference = new Reference<>();
            BuiltClass builtClass = this.buildClass(methodsReference);
            this.proxyClass = this.classDefiner.defineProxyClass(builtClass, this.superClass, this.interfaces);

            Field methods = Fields.getDeclaredField(this.proxyClass, "METHODS");
            Fields.setObject(null, methods, methodsReference.value);
        }
        return new ProxyClass(this.proxyClass, this.invocationHandler);
    }

    private BuiltClass buildClass(final Reference<Method[]> methodsReference) {
        String className = "net/lenni0451/reflect/proxy/ProxyImpl$" + System.nanoTime();
        Class<?>[] interfaces = this.interfaces;
        if (interfaces == null) {
            interfaces = new Class[]{Proxy.class};
        } else {
            interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
            interfaces[interfaces.length - 1] = Proxy.class;
        }

        return BUILDER.class_(
                BUILDER.opcode("ACC_PUBLIC"),
                className,
                null,
                this.superClass == null ? slash(Object.class) : slash(this.superClass),
                Arrays.stream(interfaces).map(BytecodeUtils::slash).toArray(String[]::new),
                cb -> {
                    this.addConstructors(cb);

                    Method[] methods = ProxyUtils.getOverridableMethod(this.superClass, this.interfaces);
                    methodsReference.value = methods;
                    this.addFields(cb, methods);
                    this.addMethods(cb, methods);
                    this.addDefaultMethods(cb);
                }
        );
    }

    private void addConstructors(final ClassBuilder cb) {
        if (this.superClass == null) {
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class), null, null, mb -> mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .method(BUILDER.opcode("INVOKESPECIAL"), slash(Object.class), "<init>", mdesc(void.class), false)
                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(1, 1)
            );
        } else {
            Constructor<?>[] constructors = ProxyUtils.getPublicConstructors(this.superClass);
            if (constructors.length == 0) throw new IllegalArgumentException("Cannot create a proxy for a class without any public constructors");
            for (Constructor<?> constructor : constructors) {
                cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, constructor.getParameterTypes()), null, null, mb -> {
                    mb.var(BUILDER.opcode("ALOAD"), 0);
                    int index = 1;
                    for (Class<?> parameter : constructor.getParameterTypes()) {
                        mb.var(BUILDER.opcode(getLoadOpcode(parameter)), index);
                        index += getStackSize(parameter);
                    }
                    mb
                            .method(BUILDER.opcode("INVOKESPECIAL"), slash(this.superClass), "<init>", mdesc(void.class, constructor.getParameterTypes()), false)
                            .insn(BUILDER.opcode("RETURN"))
                            .maxs(index, index);
                });
            }
        }
    }

    private void addFields(final ClassBuilder cb, final Method[] methods) {
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), "METHODS", desc(Method[].class), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), "invocationHandler", desc(InvocationHandler.class), null, null);
        for (int i = 0; i < methods.length; i++) {
            cb.field(BUILDER.opcode("ACC_PRIVATE"), "method" + i, desc(ProxyMethod.class), null, null);
        }
    }

    private void addMethods(final ClassBuilder cb, final Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            final int methodId = i;
            Method method = methods[i];
            cb.method(BUILDER.opcode("ACC_PUBLIC"), method.getName(), desc(method), null, null, mb -> {
                BytecodeLabel elseLabel = BUILDER.label();

                mb
                        .var(BUILDER.opcode("ALOAD"), 0) //this
                        .field(BUILDER.opcode("GETFIELD"), cb.getName(), "invocationHandler", desc(InvocationHandler.class)) //this.invocationHandler
                        .var(BUILDER.opcode("ALOAD"), 0) //this.invocationHandler, this
                        .insn(BUILDER.opcode("DUP")) //this.invocationHandler, this, this
                        .field(BUILDER.opcode("GETFIELD"), cb.getName(), "method" + methodId, desc(ProxyMethod.class)) //this.invocationHandler, this, this.methodN
                        .insn(BUILDER.opcode("DUP")) //this.invocationHandler, this, this.methodN, this.methodN
                        .jump(BUILDER.opcode("IFNONNULL"), elseLabel); //this.invocationHandler, this, this.methodN
                mb
                        .insn(BUILDER.opcode("POP")) //this.invocationHandler, this
                        .var(BUILDER.opcode("ALOAD"), 0) //this.invocationHandler, this, this
                        .insn(BUILDER.opcode("DUP")) //this.invocationHandler, this, this, this
                        .field(BUILDER.opcode("GETSTATIC"), cb.getName(), "METHODS", desc(Method[].class)) //this.invocationHandler, this, this, this, METHODS
                        .intPush(BUILDER, methodId) //this.invocationHandler, this, this, this, METHODS, methodId
                        .insn(BUILDER.opcode("AALOAD")) //this.invocationHandler, this, this, this, method
                        .method(BUILDER.opcode("INVOKESTATIC"), slash(ProxyRuntime.class), "makeProxyMethod", mdesc(ProxyMethod.class, Object.class, Method.class), false) //this.invocationHandler, this, this, proxyMethod
                        .insn(BUILDER.opcode("DUP_X1")) //this.invocationHandler, this, proxyMethod, this, proxyMethod
                        .field(BUILDER.opcode("PUTFIELD"), cb.getName(), "method" + methodId, desc(ProxyMethod.class)); //this.invocationHandler, this, proxyMethod
                mb
                        .label(elseLabel) //this.invocationHandler, this, proxyMethod
                        .intPush(BUILDER, method.getParameterCount()) //this.invocationHandler, this, proxyMethod, parameterCount
                        .type(BUILDER.opcode("ANEWARRAY"), slash(Object.class)); //this.invocationHandler, this, proxyMethod, parameters

                int paramVarIndex = 1;
                for (int param = 0; param < method.getParameterCount(); param++) {
                    Class<?> parameter = method.getParameterTypes()[param];
                    mb
                            .insn(BUILDER.opcode("DUP")) //this.invocationHandler, this, proxyMethod, parameters, parameters
                            .intPush(BUILDER, param) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex
                            .var(BUILDER.opcode(getLoadOpcode(parameter)), paramVarIndex) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex, parameterValue
                            .box(BUILDER, parameter) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex, parameterValue
                            .insn(BUILDER.opcode("AASTORE")); //this.invocationHandler, this, proxyMethod, parameters
                    paramVarIndex += getStackSize(parameter);
                }

                mb.method(BUILDER.opcode("INVOKEINTERFACE"), slash(InvocationHandler.class), "invoke", mdesc(Object.class, Object.class, ProxyMethod.class, Object[].class), true); //this.invocationHandler, this, proxyMethod, parameters
                if (method.getReturnType() == void.class) {
                    mb.insn(BUILDER.opcode("POP"));
                } else {
                    mb
                            .type(BUILDER.opcode("CHECKCAST"), slash(boxed(method.getReturnType())))
                            .unbox(BUILDER, method.getReturnType());
                }
                mb
                        .insn(BUILDER.opcode(getReturnOpcode(method.getReturnType()))) //returnValue (if not void)
                        .maxs(paramVarIndex, 1);
            });
        }
    }

    private void addDefaultMethods(final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "setInvocationHandler", mdesc(void.class, InvocationHandler.class), null, null, mb -> {
            mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .var(BUILDER.opcode("ALOAD"), 1)
                    .field(BUILDER.opcode("PUTFIELD"), cb.getName(), "invocationHandler", desc(InvocationHandler.class))
                    .insn(BUILDER.opcode("RETURN"))
                    .maxs(2, 2);
        });
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "getInvocationHandler", mdesc(InvocationHandler.class), null, null, mb -> {
            mb
                    .var(BUILDER.opcode("ALOAD"), 0)
                    .field(BUILDER.opcode("GETFIELD"), cb.getName(), "invocationHandler", desc(InvocationHandler.class))
                    .insn(BUILDER.opcode("ARETURN"))
                    .maxs(1, 1);
        });
    }

    private void reset() {
        this.proxyClass = null;
    }


    private static class Reference<T> {
        private T value;
    }

}
