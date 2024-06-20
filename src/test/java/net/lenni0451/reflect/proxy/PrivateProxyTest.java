package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.proxy.test.Class3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PrivateProxyTest {

    @Test
    void test() {
        ProxyClass proxyClass = new ProxyBuilder()
                .setSuperClass(Class3.class)
                .setInvocationHandler(InvocationHandler.cancelling())
                .build();
        Class3 instance = proxyClass.allocateInstance();
        assertNotNull(instance);
        assertNull(instance.toString());
    }

}
