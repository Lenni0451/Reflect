package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.proxy.impl.ProxyMethod;

import java.lang.reflect.Modifier;

public interface InvocationHandler {

    static InvocationHandler forwarding() {
        return (thiz, proxyMethod, args) -> {
            if (Modifier.isAbstract(proxyMethod.getInvokedMethod().getModifiers())) {
                return proxyMethod.cancel();
            } else {
                return proxyMethod.invokeSuper(args);
            }
        };
    }


    Object invoke(final Object thiz, final ProxyMethod proxyMethod, final Object... args);

}
