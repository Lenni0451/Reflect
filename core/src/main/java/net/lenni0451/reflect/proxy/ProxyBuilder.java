package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Fields;
import net.lenni0451.reflect.bytecode.BytecodeUtils;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.proxy.impl.Proxy;
import net.lenni0451.reflect.proxy.impl.ProxyMethod;
import net.lenni0451.reflect.proxy.impl.ProxyRuntime;
import net.lenni0451.reflect.proxy.internal.ProxyMethodBuilder;
import net.lenni0451.reflect.proxy.internal.ProxyUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * A builder to create proxy classes.
 */
public class ProxyBuilder {

    private static final BytecodeBuilder BUILDER = BytecodeBuilder.get();
    private static final String METHODS_FIELD = "METHODS";
    private static final String PROXY_METHOD_CLASSES_FIELD = "PROXY_METHOD_CLASSES";
    private static final String INVOCATION_HANDLER_FIELD = "invocationHandler";

    @Nullable
    private Class<?> superClass;
    @Nullable
    private Class<?>[] interfaces;
    private Predicate<Method> methodFilter = m -> true;
    private Function<Method, Method> methodMapper = Function.identity();
    private InvocationHandler invocationHandler = InvocationHandler.forwarding();
    private ProxyClassDefiner classDefiner = ProxyClassDefiner.loader(ProxyBuilder.class.getClassLoader());

    private Class<?> proxyClass;

    /**
     * @return The super class of the proxy class
     */
    @Nullable
    public Class<?> getSuperClass() {
        return this.superClass;
    }

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
     * @return The interfaces of the proxy class
     */
    @Nullable
    public Class<?>[] getInterfaces() {
        return this.interfaces;
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
     * @return The current method filter
     */
    public Predicate<Method> getMethodFilter() {
        return this.methodFilter;
    }

    /**
     * Set the filter for the methods that should be overridden by the proxy class.<br>
     * Filtered methods will not be overridden by the proxy class.<br>
     * Abstract methods will always be overridden and cannot be filtered.
     *
     * @param methodFilter The method filter
     * @return This builder
     */
    public ProxyBuilder setMethodFilter(@Nonnull final Predicate<Method> methodFilter) {
        this.reset();
        this.methodFilter = methodFilter;
        return this;
    }

    /**
     * @return The current method mapper
     */
    public Function<Method, Method> getMethodMapper() {
        return this.methodMapper;
    }

    /**
     * Set the mapper for the methods that should be overridden by the proxy class.<br>
     * The mapper can be used to change the owner of an overridden method.<br>
     * The owner must be a super class of the original owner.<br>
     * The super call will not be changed by the mapper.
     *
     * @param methodMapper The method mapper
     * @return This builder
     */
    public ProxyBuilder setMethodMapper(@Nonnull final Function<Method, Method> methodMapper) {
        this.reset();
        this.methodMapper = methodMapper;
        return this;
    }

    /**
     * @return The invocation handler
     */
    public InvocationHandler getInvocationHandler() {
        return this.invocationHandler;
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
     * @return The current class definer
     */
    public ProxyClassDefiner getClassDefiner() {
        return this.classDefiner;
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
     * Build the proxy class.
     *
     * @return The built proxy class
     */
    public ProxyClass build() {
        if (this.proxyClass == null) {
            Reference<Method[]> methodsReference = new Reference<>();
            Reference<Method[]> originalMethodsReference = new Reference<>();
            BuiltClass builtClass = this.buildClass(methodsReference, originalMethodsReference);
            this.proxyClass = this.classDefiner.defineProxyClass(builtClass, this.superClass, this.interfaces);

            //Set the static fields
            Field methods = Fields.getDeclaredField(this.proxyClass, METHODS_FIELD);
            Fields.setObject(null, methods, methodsReference.value);

            Class<ProxyMethod>[] proxyMethodClasses = this.buildProxyMethodClasses(methodsReference.value, originalMethodsReference.value);
            Field proxyMethodClassesField = Fields.getDeclaredField(this.proxyClass, PROXY_METHOD_CLASSES_FIELD);
            Fields.setObject(null, proxyMethodClassesField, proxyMethodClasses);
        }
        return new ProxyClass(this.proxyClass, this.invocationHandler);
    }

    private BuiltClass buildClass(final Reference<Method[]> methodsReference, final Reference<Method[]> originalMethodsReference) {
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

                    Method[] methods = ProxyUtils.getOverridableMethod(this.superClass, this.interfaces, this.methodFilter);
                    methodsReference.value = methods;
                    originalMethodsReference.value = ProxyUtils.mapMethods(methods, this.methodMapper);
                    this.addFields(cb, methods);
                    this.addMethods(cb, methods);
                    this.addDefaultMethods(cb);
                }
        );
    }

    private void addConstructors(final ClassBuilder cb) {
        if (this.superClass == null) {
            cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class), null, null, mb -> mb
                    .aload(0)
                    .invokespecial(slash(Object.class), "<init>", mdesc(void.class), false)
                    .return_()
                    .maxs(1, 1)
            );
        } else {
            Constructor<?>[] constructors = ProxyUtils.getPublicConstructors(this.superClass);
            for (Constructor<?> constructor : constructors) {
                cb.method(BUILDER.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, constructor.getParameterTypes()), null, null, mb -> {
                    mb.aload(0);
                    int index = 1;
                    for (Class<?> parameter : constructor.getParameterTypes()) {
                        mb.load(parameter, index);
                        index += getStackSize(parameter);
                    }
                    mb
                            .invokespecial(slash(this.superClass), "<init>", mdesc(void.class, constructor.getParameterTypes()), false)
                            .return_()
                            .maxs(index, index);
                });
            }
        }
    }

    private void addFields(final ClassBuilder cb, final Method[] methods) {
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), METHODS_FIELD, desc(Method[].class), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_STATIC", "ACC_FINAL"), PROXY_METHOD_CLASSES_FIELD, desc(Class[].class), null, null);
        cb.field(BUILDER.opcode("ACC_PRIVATE", "ACC_FINAL"), INVOCATION_HANDLER_FIELD, desc(InvocationHandler.class), null, null);
        for (int i = 0; i < methods.length; i++) {
            cb.field(BUILDER.opcode("ACC_PRIVATE"), "method" + i, desc(ProxyMethod.class), null, null);
        }
    }

    private void addMethods(final ClassBuilder cb, final Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            final int methodId = i;
            Method method = methods[i];
            cb.method(BUILDER.opcode("ACC_PUBLIC"), method.getName(), desc(method), null, null, mb -> {
                BytecodeLabel elseLabel = mb.newLabel();

                mb
                        .aload(0) //this
                        .getfield(cb.getName(), INVOCATION_HANDLER_FIELD, desc(InvocationHandler.class)) //this.invocationHandler
                        .aload(0) //this.invocationHandler, this
                        .dup() //this.invocationHandler, this, this
                        .getfield(cb.getName(), "method" + methodId, desc(ProxyMethod.class)) //this.invocationHandler, this, this.methodN
                        .dup() //this.invocationHandler, this, this.methodN, this.methodN
                        .ifnonnull(elseLabel) //this.invocationHandler, this, this.methodN
                        .pop(); //this.invocationHandler, this
                mb
                        .aload(0) //^, this
                        .getstatic(cb.getName(), PROXY_METHOD_CLASSES_FIELD, desc(Class[].class)) //^, this, PROXY_METHOD_CLASSES
                        .intPush(methodId) //^, this, PROXY_METHOD_CLASSES, methodId
                        .aaload() //^, this, proxyMethodClass
                        .aload(0) //^, this, proxyMethodClass, this
                        .getstatic(cb.getName(), METHODS_FIELD, desc(Method[].class)) //^, this, proxyMethodClass, this, METHODS
                        .intPush(methodId) //^, this, proxyMethodClass, this, METHODS, methodId
                        .aaload() //^, this, proxyMethodClass, this, method
                        .invokestatic(slash(ProxyRuntime.class), "instantiateProxyMethod", mdesc(ProxyMethod.class, Class.class, Object.class, Method.class), false) //^, this, proxyMethod
                        .dupX1() //^, proxyMethod, this, proxyMethod
                        .putfield(cb.getName(), "method" + methodId, desc(ProxyMethod.class)); //^, proxyMethod
                mb
                        .label(elseLabel) //this.invocationHandler, this, proxyMethod
                        .intPush(method.getParameterCount()) //this.invocationHandler, this, proxyMethod, parameterCount
                        .anewarray(slash(Object.class)); //this.invocationHandler, this, proxyMethod, parameters

                int paramVarIndex = 1;
                for (int param = 0; param < method.getParameterCount(); param++) {
                    Class<?> parameter = method.getParameterTypes()[param];
                    mb
                            .dup() //this.invocationHandler, this, proxyMethod, parameters, parameters
                            .intPush(param) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex
                            .load(parameter, paramVarIndex) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex, parameterValue
                            .box(parameter) //this.invocationHandler, this, proxyMethod, parameters, parameters, parameterIndex, parameterValue
                            .aastore(); //this.invocationHandler, this, proxyMethod, parameters
                    paramVarIndex += getStackSize(parameter);
                }

                mb.invokeinterface(slash(InvocationHandler.class), "invoke", mdesc(Object.class, Object.class, ProxyMethod.class, Object[].class)); //this.invocationHandler, this, proxyMethod, parameters
                if (method.getReturnType() == void.class) {
                    mb.pop();
                } else {
                    mb
                            .checkcast(slash(boxed(method.getReturnType())))
                            .unbox(method.getReturnType());
                }
                mb
                        .return_(method.getReturnType()) //returnValue (if not void)
                        .maxs(paramVarIndex, 1);
            });
        }
    }

    private void addDefaultMethods(final ClassBuilder cb) {
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "setInvocationHandler", mdesc(void.class, InvocationHandler.class), null, null, mb -> {
            mb
                    .aload(0)
                    .aload(1)
                    .putfield(cb.getName(), INVOCATION_HANDLER_FIELD, desc(InvocationHandler.class))
                    .return_()
                    .maxs(2, 2);
        });
        cb.method(BUILDER.opcode("ACC_PUBLIC"), "getInvocationHandler", mdesc(InvocationHandler.class), null, null, mb -> {
            mb
                    .aload(0)
                    .getfield(cb.getName(), INVOCATION_HANDLER_FIELD, desc(InvocationHandler.class))
                    .areturn()
                    .maxs(1, 1);
        });
    }

    private Class<ProxyMethod>[] buildProxyMethodClasses(final Method[] methods, final Method[] originalMethods) {
        Class<ProxyMethod>[] proxyMethodClasses = new Class[methods.length];
        for (int i = 0; i < methods.length; i++) {
            proxyMethodClasses[i] = ProxyMethodBuilder.buildProxyMethodClass(this.proxyClass, methods[i], originalMethods[i]);
        }
        return proxyMethodClasses;
    }

    private void reset() {
        this.proxyClass = null;
    }


    private static class Reference<T> {
        private T value;
    }

}
