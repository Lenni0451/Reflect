package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Constructors;
import net.lenni0451.reflect.Objects;
import net.lenni0451.reflect.exceptions.ConstructorNotFoundException;
import net.lenni0451.reflect.proxy.impl.Proxy;

import java.lang.reflect.Constructor;

/**
 * Represents a built proxy class.<br>
 * This class is used to create an instance of the proxy class.
 */
public class ProxyClass {

    private final Class<?> proxyClass;
    private InvocationHandler invocationHandler;

    public ProxyClass(final Class<?> proxyClass, final InvocationHandler invocationHandler) {
        this.proxyClass = proxyClass;
        this.invocationHandler = invocationHandler;
    }

    /**
     * @return The built proxy class
     */
    public Class<?> getProxyClass() {
        return this.proxyClass;
    }

    /**
     * @return The invocation handler
     */
    public InvocationHandler getInvocationHandler() {
        return this.invocationHandler;
    }

    /**
     * Sets the invocation handler for the proxy class.
     *
     * @param invocationHandler The invocation handler
     */
    public void setInvocationHandler(final InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    /**
     * Allocate a new instance of the proxy class without calling a constructor.<br>
     * The invocation handler will be set automatically.
     *
     * @param <T> The type of the proxy class
     * @return The new instance of the proxy class
     */
    public <T> T allocateInstance() {
        Object instance = Objects.allocate(this.proxyClass);
        ((Proxy) instance).setInvocationHandler(this.invocationHandler);
        return (T) instance;
    }

    /**
     * Instantiate a new instance of the proxy class with the given constructor parameters and arguments.<br>
     * The invocation handler will be set automatically.
     *
     * @param constructorParameters The parameters of the constructor
     * @param constructorArguments  The arguments for the constructor
     * @param <T>                   The type of the proxy class
     * @return The new instance of the proxy class
     */
    public <T> T instantiate(final Class<?>[] constructorParameters, final Object[] constructorArguments) {
        Constructor<?> constructor = Constructors.getDeclaredConstructor(this.proxyClass, constructorParameters);
        if (constructor == null) throw new ConstructorNotFoundException("Proxy", constructorParameters);
        Object instance = Constructors.invoke(constructor, constructorArguments);
        ((Proxy) instance).setInvocationHandler(this.invocationHandler);
        return (T) instance;
    }

}
