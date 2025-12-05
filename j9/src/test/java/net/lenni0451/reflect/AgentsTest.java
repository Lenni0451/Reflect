package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;

class AgentsTest {

    @Test
    void getInstrumentation() {
        Instrumentation instrumentation = assertDoesNotThrow(Agents::getInstrumentation);
        assertNotNull(instrumentation);
        assertTrue(instrumentation.getAllLoadedClasses().length > 0);
        assertTrue(instrumentation.isRetransformClassesSupported());
        assertTrue(instrumentation.isRedefineClassesSupported());
        assertTrue(instrumentation.isNativeMethodPrefixSupported());
    }

}
