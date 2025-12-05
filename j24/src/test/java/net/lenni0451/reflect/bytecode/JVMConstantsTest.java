package net.lenni0451.reflect.bytecode;

import net.lenni0451.reflect.JVMConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JVMConstantsTest {

    @Test
    void javaVersion() {
        assertEquals(24, JVMConstants.JAVA_VERSION);
    }

}
