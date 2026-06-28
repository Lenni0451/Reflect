package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Classes$MRTest {

    @Test
    void getCallerClass() {
        assertEquals(Classes$MRTest.class, TestClasses.getCallerClass(0));
        assertNull(TestClasses.getCallerClass(100_000));
    }


    static class TestClasses {
        /**
         * Base implementation of {@link Classes#getCallerClass(int)}.<br>
         * Normally the Classes method should be called, but this doesn't work in the test environment because of multi release jars.
         */
        static Class<?> getCallerClass(final int depth) {
            return Classes$MR.getCallerClass(depth);
        }
    }

}
