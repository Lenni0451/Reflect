package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentsTest {

    @Test
    void getInstrumentation() {
        assertThrows(IllegalStateException.class, Agents::getInstrumentation);
    }

}
