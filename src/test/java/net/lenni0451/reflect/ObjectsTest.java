package net.lenni0451.reflect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectsTest {

    @Test
    void toFromAddress() {
        String s = "Hello World";
        long address = assertDoesNotThrow(() -> Objects.toJvmAddress(s));
        String s2 = assertDoesNotThrow(() -> Objects.fromJvmAddress(address));
        assertEquals(s, s2);
    }

    @Test
    void cast() {
        if (JVMConstants.OPENJ9_RUNTIME) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> Objects.cast(new byte[0], CustomByteArrayOutputStream.class));
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Objects.cast(baos, CustomByteArrayOutputStream.class);
            assertEquals("Hello World", baos.toString());
        }
    }


    private static class CustomByteArrayOutputStream extends ByteArrayOutputStream {
        @Override
        public synchronized String toString() {
            return "Hello World";
        }
    }

}
