package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModulesTest {

    @Test
    void copyModule() {
        assertDoesNotThrow(() -> Modules.copyModule(Object.class, ModuleHolder.class));
    }

    @Test
    void openModule() {
        assertDoesNotThrow(() -> Modules.openModule(ModulesTest.class));
    }


    private static class ModuleHolder {
    }

}
