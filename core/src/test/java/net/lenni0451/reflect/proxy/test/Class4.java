package net.lenni0451.reflect.proxy.test;

import net.lenni0451.reflect.proxy.InvocationHandler;
import net.lenni0451.reflect.proxy.impl.Proxy;

public abstract class Class4 implements Proxy {

    public InvocationHandler get() {
        return this.getInvocationHandler();
    }

}
