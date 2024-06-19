package net.lenni0451.reflect.proxy.impl;

import java.lang.reflect.Method;

public interface ProxyMethod {

    Method getInvokedMethod();

    Object invokeWith(final Object instance, final Object... args);

    Object invokeSuper(final Object... args);

    Object cancel();

}
