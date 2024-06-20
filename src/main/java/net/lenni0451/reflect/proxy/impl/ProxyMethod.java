package net.lenni0451.reflect.proxy.impl;

import java.lang.reflect.Method;

/**
 * Represents a proxied method call.<br>
 * The original call can be forwarded to the super class, forwarded to another instance or cancelled.
 */
public interface ProxyMethod {

    /**
     * @return The invoked method
     */
    Method getInvokedMethod();

    /**
     * Invoke the proxied method with the given instance and arguments.
     *
     * @param instance The instance to invoke the method on
     * @param args     The arguments to pass to the method
     * @return The result of the method call
     */
    Object invokeWith(final Object instance, final Object... args);

    /**
     * Invoke the proxied method on the proxy super class with the given arguments.<br>
     * This will not work if the super method is abstract.
     *
     * @param args The arguments to pass to the method
     * @return The result of the method call
     */
    Object invokeSuper(final Object... args);

    /**
     * Get the default return value of the method.<br>
     * This can be used to cancel the method call by returning the default.
     *
     * @return The default return value
     */
    Object cancel();

}
