package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.proxy.impl.Proxy;
import net.lenni0451.reflect.proxy.test.Class1;
import net.lenni0451.reflect.proxy.test.Class2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProxyTest {

    private Class1 proxy;

    @BeforeEach
    void init() {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setSuperClass(Class1.class);
        this.proxy = proxyBuilder.build().allocateInstance();
    }

    @Test
    void testChangeReturnValue() {
        ((Proxy) this.proxy).setInvocationHandler((thiz, proxyMethod, args) -> 10);
        assertEquals(10, this.proxy.test());
    }

    @Test
    void testForward() {
        ((Proxy) this.proxy).setInvocationHandler(InvocationHandler.forwarding());
        assertEquals(1, this.proxy.test());
        assertEquals(10L, this.proxy.conv((byte) 10));
    }

    @Test
    void testInvokeOther() {
        Class2 other = new Class2();
        ((Proxy) this.proxy).setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeWith(other, args));
        assertEquals(2, this.proxy.test());
    }

    @Test
    void testArgTypes() {
        this.proxy.takeAll(true, (byte) 1, (short) 2, 'c', 3, 4L, 5F, 6D, "test");
    }

    @Test
    void testCancel() {
        ((Proxy) this.proxy).setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.cancel());
        assertFalse(this.proxy.getBoolean());
        assertEquals(0, this.proxy.getByte());
        assertEquals(0, this.proxy.getShort());
        assertEquals(0, this.proxy.getChar());
        assertEquals(0, this.proxy.getInt());
        assertEquals(0, this.proxy.getLong());
        assertEquals(0, this.proxy.getFloat());
        assertEquals(0, this.proxy.getDouble());
        assertNull(this.proxy.getString());
    }

}
