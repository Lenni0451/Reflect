package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.proxy.impl.ProxyMethod;

import java.lang.reflect.Modifier;

/**
 * The handler which is called when a method of a proxy class is invoked.
 */
public interface InvocationHandler {

    /**
     * Get a new invocation handler which forwards all method calls to the super class.<br>
     * If the method is abstract it will be cancelled.
     *
     * @return The new invocation handler
     */
    static InvocationHandler forwarding() {
        return (thiz, proxyMethod, args) -> {
            if (Modifier.isAbstract(proxyMethod.getInvokedMethod().getModifiers())) {
                return proxyMethod.cancel();
            } else {
                return proxyMethod.invokeSuper(args);
            }
        };
    }

    /**
     * Get a new invocation handler which cancels all method calls.
     *
     * @return The new invocation handler
     */
    static InvocationHandler cancelling() {
        return (thiz, proxyMethod, args) -> proxyMethod.cancel();
    }


    /**
     * Handle a proxy method invocation.<br>
     * The method can be forwarded to the super class, another instance or cancelled.
     *
     * @param thiz        The instance of the proxy
     * @param proxyMethod The proxy method which was invoked
     * @param args        The arguments of the method call
     * @return The result of the method call
     */
    Object invoke(final Object thiz, final ProxyMethod proxyMethod, final Object... args);

}
