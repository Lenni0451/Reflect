package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MethodsTest {

    @Test
    void getDeclaredMethods() {
        Method[] methods = assertDoesNotThrow(() -> Methods.getDeclaredMethods(Methods.class));
        assertTrue(methods.length > 0);
    }

    @Test
    void invoke() {
        String s = "Hello World";
        Method method = assertDoesNotThrow(() -> String.class.getDeclaredMethod("toString"));
        String result = assertDoesNotThrow(() -> Methods.invoke(s, method));
        assertEquals(s, result);
    }

    @Test
    void invokeSuper() {
        String s = "Hello World";
        Method method = assertDoesNotThrow(() -> Object.class.getDeclaredMethod("toString"));
        String result = assertDoesNotThrow(() -> Methods.invokeSuper(s, Object.class, method));
        assertNotEquals(s, result);
    }

}
