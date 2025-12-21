package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JVMConstantsTest {

    @Test
    void javaVersion() {
        assertEquals(24, JVMConstants.JAVA_VERSION);
    }

}
