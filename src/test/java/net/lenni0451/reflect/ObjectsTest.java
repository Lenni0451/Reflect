package net.lenni0451.reflect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectsTest {

    @Test
    void toFromAddress() {
        String in = "Hello World";
        long jvmAddress = assertDoesNotThrow(() -> Objects.toJVMAddress(in));
        long nativeAddress = assertDoesNotThrow(() -> Objects.toNativeAddress(in));
        assertEquals(jvmAddress, Objects.toJVMAddress(nativeAddress));
        assertEquals(nativeAddress, Objects.toNativeAddress(jvmAddress));

        String fromJVMAddress = assertDoesNotThrow(() -> Objects.fromJVMAddress(jvmAddress));
        String fromNativeAddress = assertDoesNotThrow(() -> Objects.fromJVMAddress(Objects.toJVMAddress(nativeAddress)));
        assertEquals(in, fromJVMAddress);
        assertEquals(in, fromNativeAddress);
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
