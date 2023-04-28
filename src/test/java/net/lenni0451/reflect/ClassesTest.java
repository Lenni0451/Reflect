package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassesTest {

    @Test
    void getDeclaredClasses() {
        assertTrue(Classes.getDeclaredClasses(Class.class).length > 0);
    }

    @Test
    void byName() {
        assertNotNull(assertDoesNotThrow(() -> Classes.byName("java.lang.String")));
        assertNotNull(assertDoesNotThrow(() -> Classes.byName("java.lang.String", null)));
        assertNotNull(assertDoesNotThrow(() -> Classes.byName("java.lang.String", true, null)));
    }

}
