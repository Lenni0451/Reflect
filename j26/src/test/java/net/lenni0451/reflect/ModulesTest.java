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

    @Test
    void testOpenModule() {
        assertDoesNotThrow(() -> Modules.openModule(ModulesTest.class, ModulesTest.class.getPackageName()));
    }

    @Test
    void openEntireModule() {
        assertDoesNotThrow(() -> Modules.openEntireModule(ModulesTest.class));
    }

    @Test
    void enableNativeAccess() {
        assertDoesNotThrow(() -> Modules.enableNativeAccess(ModulesTest.class));
    }

    @Test
    void enableNativeAccessToAllUnnamed() {
        assertDoesNotThrow(Modules::enableNativeAccessToAllUnnamed);
    }


    private static class ModuleHolder {
    }

}
