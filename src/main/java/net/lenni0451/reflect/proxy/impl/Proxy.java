package net.lenni0451.reflect.proxy.impl;

import net.lenni0451.reflect.proxy.InvocationHandler;

import javax.annotation.Nonnull;

/**
 * Represents a proxy class.
 */
public interface Proxy {

    /**
     * Set the invocation handler for handling the method calls.
     *
     * @param invocationHandler The invocation handler
     */
    void setInvocationHandler(@Nonnull final InvocationHandler invocationHandler);

}
