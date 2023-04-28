package net.lenni0451.reflect.accessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodAccessorTest {

    private MethodClass mc;
    private Method method;

    @BeforeEach
    void setUp() {
        this.mc = new MethodClass();
        this.method = assertDoesNotThrow(() -> MethodClass.class.getDeclaredMethod("reverse", String.class));
    }

    @Test
    void makeInvoker() {
        Function<String, String> invoker = assertDoesNotThrow(() -> MethodAccessor.makeInvoker(Function.class, this.mc, this.method));
        assertEquals("cba", invoker.apply("abc"));
    }

    @Test
    void makeDynamicInvoker() {
        BiFunction<MethodClass, String, String> dynamicInvoker = assertDoesNotThrow(() -> MethodAccessor.makeDynamicInvoker(BiFunction.class, this.method));
        assertEquals("cba", dynamicInvoker.apply(this.mc, "abc"));
    }


    private static class MethodClass {
        private String reverse(final String s) {
            char[] chars = s.toCharArray();
            char[] reversed = new char[chars.length];
            for (int i = 0; i < chars.length; i++) reversed[i] = chars[chars.length - i - 1];
            return new String(reversed);
        }
    }

}
