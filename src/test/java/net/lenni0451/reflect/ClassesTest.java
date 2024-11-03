package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.util.Collections;

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
        assertNull(assertDoesNotThrow(() -> Classes.byName("UnknownClass")));
        assertNull(assertDoesNotThrow(() -> Classes.byName("UnknownClass", null)));
        assertNull(assertDoesNotThrow(() -> Classes.byName("UnknownClass", true, null)));
    }

    @Test
    void forName() {
        assertNotNull(assertDoesNotThrow(() -> Classes.forName("java.lang.String")));
        assertNotNull(assertDoesNotThrow(() -> Classes.forName("java.lang.String", null)));
        assertNotNull(assertDoesNotThrow(() -> Classes.forName("java.lang.String", true, null)));
        assertThrows(ClassNotFoundException.class, () -> Classes.forName("UnknownClass"));
        assertThrows(ClassNotFoundException.class, () -> Classes.forName("UnknownClass", null));
        assertThrows(ClassNotFoundException.class, () -> Classes.forName("UnknownClass", true, null));
    }

    @Test
    void find() {
        assertThrows(ClassNotFoundException.class, () -> Classes.find("UnknownClass", true, ClassLoader.getSystemClassLoader()));
        assertEquals(String.class, assertDoesNotThrow(() -> Classes.find("java.lang.String", true, ClassLoader.getSystemClassLoader())));
        assertThrows(ClassNotFoundException.class, () -> Classes.find("UnknownClass", true, Collections.singletonList(ClassLoader.getSystemClassLoader())));
        assertEquals(String.class, assertDoesNotThrow(() -> Classes.find("java.lang.String", true, Collections.singletonList(ClassLoader.getSystemClassLoader()))));
    }

}
