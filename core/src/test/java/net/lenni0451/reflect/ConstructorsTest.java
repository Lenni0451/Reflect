package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorsTest {

    @Test
    void getDeclaredConstructors() {
        Constructor<System>[] constructors = assertDoesNotThrow(() -> Constructors.getDeclaredConstructors(System.class));
        assertTrue(constructors.length > 0);
    }

    @Test
    void invoke() {
        Constructor<System> constructor = assertDoesNotThrow(() -> Constructors.getDeclaredConstructor(System.class));
        assertNotNull(constructor);
        System system = assertDoesNotThrow(() -> Constructors.invoke(constructor));
        assertNotNull(system);
    }

    @Test
    void makeInvokable() {
        MethodHandle handle = assertDoesNotThrow(() -> JavaBypass.TRUSTED_LOOKUP.findConstructor(String.class, MethodType.methodType(void.class, char[].class)));
        MethodHandle invokable = assertDoesNotThrow(() -> Constructors.makeInvokable(handle));
        String s = "Hello!";
        assertDoesNotThrow(() -> {
            invokable.invokeExact(s, new char[]{'B', 'y', 'e', '!'});
        });
        assertEquals("Bye!", s);
    }

}
