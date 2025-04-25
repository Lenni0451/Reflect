package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Classes$MRTest {

    @Test
    void getCallerClass() {
        assertEquals(Classes$MRTest.class, TestClasses.getCallerClass(0));
    }


    private static class TestClasses {
        /**
         * Base implementation of {@link Classes#getCallerClass(int)}.<br>
         * Normally the Classes method should be called, but this doesn't work in the test environment because of multi release jars.
         */
        private static Class<?> getCallerClass(final int depth) {
            return Classes$MR.getCallerClass(depth);
        }
    }

}
