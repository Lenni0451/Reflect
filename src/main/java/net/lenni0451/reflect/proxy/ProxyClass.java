package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Objects;
import net.lenni0451.reflect.exceptions.ConstructorNotFoundException;
import net.lenni0451.reflect.proxy.impl.Proxy;

import java.lang.reflect.Constructor;

public class ProxyClass {

    private final Class<?> proxyClass;
    private InvocationHandler invocationHandler;

    public ProxyClass(final Class<?> proxyClass, final InvocationHandler invocationHandler) {
        this.proxyClass = proxyClass;
        this.invocationHandler = invocationHandler;
    }

    public Class<?> getProxyClass() {
        return this.proxyClass;
    }

    public InvocationHandler getInvocationHandler() {
        return this.invocationHandler;
    }

    public void setInvocationHandler(final InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    public <T> T allocateInstance() {
        Object instance = Objects.allocate(this.proxyClass);
        ((Proxy) instance).setInvocationHandler(this.invocationHandler);
        return (T) instance;
    }

    public <T> T instantiate(final Class<?>[] constructorParameters, final Object[] constructorArguments) {
        Constructor<?> constructor = Constructors.getDeclaredConstructor(this.proxyClass, constructorParameters);
        if (constructor == null) throw new ConstructorNotFoundException("Proxy", constructorParameters);
        Object instance = Constructors.invoke(constructor, constructorArguments);
        ((Proxy) instance).setInvocationHandler(this.invocationHandler);
        return (T) instance;
    }

}
