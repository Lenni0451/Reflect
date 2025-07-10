package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;

import static net.lenni0451.reflect.Tests.JAVA_MAJOR_VERSION;
import static org.junit.jupiter.api.Assertions.*;

class JavaBypassTest {

    @Test
    void getUnsafe() {
        Unsafe unsafe = assertDoesNotThrow(JavaBypass::getUnsafe);
        assertNotNull(unsafe);
        assertDoesNotThrow(() -> assertInstanceOf(String.class, unsafe.allocateInstance(String.class)));
    }

    @Test
    void getTrustedLookup() {
        MethodHandles.Lookup trustedLookup = assertDoesNotThrow(JavaBypass::getTrustedLookup);
        assertNotNull(trustedLookup);
    }

    @Test
    void getInternalUnsafe() {
        Object internalUnsafe = assertDoesNotThrow(JavaBypass::getInternalUnsafe);
        if (JAVA_MAJOR_VERSION < 11) assertNull(internalUnsafe); //The internal unsafe was added with java 11
        else assertNotNull(internalUnsafe);
    }

    @Test
    void clearReflectionFilter() {
        assertDoesNotThrow(JavaBypass::clearReflectionFilter);
        assertNotNull(assertDoesNotThrow(() -> Class.class.getDeclaredField("classLoader")));
    }

}
