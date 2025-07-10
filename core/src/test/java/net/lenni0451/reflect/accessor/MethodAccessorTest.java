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
    private Method method1;
    private Method method2;

    @BeforeEach
    void setUp() {
        this.mc = new MethodClass();
        this.method1 = assertDoesNotThrow(() -> MethodClass.class.getDeclaredMethod("reverse", String.class));
        this.method2 = assertDoesNotThrow(() -> MethodClass.class.getDeclaredMethod("add", String.class, int.class, double.class));
    }

    @Test
    void makeInvoker() {
        Function<String, String> invoker = assertDoesNotThrow(() -> MethodAccessor.makeInvoker(Function.class, this.mc, this.method1));
        assertEquals("cba", invoker.apply("abc"));
    }

    @Test
    void makeArrayInvoker() {
        Function<Object[], Integer> arrayInvoker = assertDoesNotThrow(() -> MethodAccessor.makeArrayInvoker(this.mc, this.method2));
        assertEquals(6, arrayInvoker.apply(new Object[]{"abc", 1, 2.78D}));
    }

    @Test
    void makeDynamicInvoker() {
        BiFunction<MethodClass, String, String> dynamicInvoker = assertDoesNotThrow(() -> MethodAccessor.makeDynamicInvoker(BiFunction.class, this.method1));
        assertEquals("cba", dynamicInvoker.apply(this.mc, "abc"));
    }

    @Test
    void makeDynamicArrayInvoker() {
        BiFunction<MethodClass, Object[], Integer> dynamicArrayInvoker = assertDoesNotThrow(() -> MethodAccessor.makeDynamicArrayInvoker(this.method2));
        assertEquals(6, dynamicArrayInvoker.apply(this.mc, new Object[]{"abc", 1, 2.78D}));
    }


    private static class MethodClass {
        private String reverse(final String s) {
            char[] chars = s.toCharArray();
            char[] reversed = new char[chars.length];
            for (int i = 0; i < chars.length; i++) reversed[i] = chars[chars.length - i - 1];
            return new String(reversed);
        }

        private int add(final String s, final int a, final double b) {
            return s.length() + a + (int) b;
        }
    }

}
