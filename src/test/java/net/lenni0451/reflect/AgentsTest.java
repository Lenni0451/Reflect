package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;

class AgentsTest {

    @Test
    void getInstrumentation() {
        if (Tests.JAVA_MAJOR_VERSION < 9) {
            assertThrows(IllegalStateException.class, Agents::getInstrumentation);
        } else {
            Instrumentation instrumentation = assertDoesNotThrow(Agents::getInstrumentation);
            assertNotNull(instrumentation);
            assertNotNull(instrumentation.getAllLoadedClasses());
            assertTrue(instrumentation.isRetransformClassesSupported());
            assertTrue(instrumentation.isRedefineClassesSupported());
            assertTrue(instrumentation.isNativeMethodPrefixSupported());
        }
    }

}
