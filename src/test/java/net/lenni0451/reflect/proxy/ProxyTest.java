package net.lenni0451.reflect.proxy;

import net.lenni0451.reflect.Methods;
import net.lenni0451.reflect.proxy.impl.Proxy;
import net.lenni0451.reflect.proxy.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ProxyTest {

    private Class1 proxy;

    @BeforeEach
    void setUp() {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setSuperClass(Class1.class);
        proxyBuilder.addInterface(Interface1.class);
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

    @Test
    void testInterface() {
        Class2 other = new Class2();
        ((Proxy) this.proxy).setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeWith(other, args));
        assertEquals(12, ((Interface1) this.proxy).interfaceTest());
    }

    @Test
    void testInvokeAbstractSuper() {
        ((Proxy) this.proxy).setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeSuper(args));
        assertThrows(AbstractMethodError.class, () -> ((Interface1) this.proxy).interfaceTest());
    }

    @Test
    void testDefaultMethods() {
        InvocationHandler handler = InvocationHandler.forwarding();
        ((Proxy) this.proxy).setInvocationHandler(handler);
        assertEquals(handler, ((Proxy) this.proxy).getInvocationHandler());
    }

    @Test
    void testNoConstructorProxy() {
        ProxyClass proxyClass = new ProxyBuilder()
                .setSuperClass(Class3.class)
                .setInvocationHandler(InvocationHandler.cancelling())
                .build();
        Class3 instance = proxyClass.allocateInstance();
        assertNotNull(instance);
        assertNull(instance.toString());
    }

    @Test
    void testSuperImplementsProxy() {
        ProxyClass proxyClass = assertDoesNotThrow(() -> new ProxyBuilder()
                .setSuperClass(Class4.class)
                .setInvocationHandler(InvocationHandler.forwarding())
                .build());
        Class4 instance = proxyClass.allocateInstance();
        assertDoesNotThrow(instance::get);
    }

    @Test
    void testInvokeWrongObject() {
        ProxyClass proxyClass = new ProxyBuilder()
                .setSuperClass(Class2.class)
                .setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeWith(new Class1(), args))
                .build();
        Class2 proxy = proxyClass.allocateInstance();
        //The method found by the proxy builder belongs to Class2 (the proxy super class)
        //Invoking it with an instance of Class1 should throw an exception because Class1 does not extend Class2, even tho it is the other way round
        //The cast is done by the proxy to allow calling invokeExact of the method handle
        assertThrows(ClassCastException.class, proxy::test);
    }

    @Test
    void testMethodMapper() {
        ProxyClass proxyClass = new ProxyBuilder()
                .setSuperClass(Class2.class)
                .setMethodMapper(method -> {
                    Method class1Method = Methods.getDeclaredMethod(Class1.class, method.getName(), method.getParameterTypes());
                    if (class1Method != null) return class1Method;
                    return method;
                })
                .setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeWith(new Class1(), args))
                .build();
        Class2 proxy = proxyClass.allocateInstance();
        //The invocation is the same as in testInvokeWrongObject but this time the method mapper maps the method to one owned by Class1
        //This should work because Class2 extends Class1 and the correct method should be invoked
        assertEquals(1, proxy.test());

        ((Proxy) proxy).setInvocationHandler(InvocationHandler.forwarding());
        //Here it should return 2 because the super method is still the one from Class2
        assertEquals(2, proxy.test());

        ((Proxy) proxy).setInvocationHandler((thiz, proxyMethod, args) -> proxyMethod.invokeWith(new Class2(), args));
        //This should also work because it's forwarded to Class2
        assertEquals(2, proxy.test());
    }

}
