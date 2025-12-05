package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JavaBypassTest {

    @Test
    void getInternalUnsafe() {
        Object internalUnsafe = assertDoesNotThrow(JavaBypass::getInternalUnsafe);
        assertNotNull(internalUnsafe);
    }

}
