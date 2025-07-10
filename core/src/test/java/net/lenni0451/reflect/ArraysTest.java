package net.lenni0451.reflect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArraysTest {

    @Test
    void setLength() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        byte[] array = new byte[10];
        Assertions.assertEquals(10, array.length);
        Arrays.setLength(array, 5);
        Assertions.assertEquals(5, array.length);
    }

    @Test
    void fillByteArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        byte[] expected = new byte[10];
        java.util.Arrays.fill(expected, (byte) (Byte.MAX_VALUE - 1));

        byte[] array = new byte[10];
        Arrays.fill(array, (byte) (Byte.MAX_VALUE - 1));
        Assertions.assertArrayEquals(expected, array);
    }

    @Test
    void fillShortArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        short[] expected = new short[10];
        java.util.Arrays.fill(expected, (short) (Short.MAX_VALUE - 1));

        short[] array = new short[10];
        Arrays.fill(array, (short) (Short.MAX_VALUE - 1));
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, (short) 0);
        Assertions.assertArrayEquals(new short[10], array);
    }

    @Test
    void fillCharArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        char[] expected = new char[10];
        java.util.Arrays.fill(expected, (char) (Character.MAX_VALUE - 1));

        char[] array = new char[10];
        Arrays.fill(array, (char) (Character.MAX_VALUE - 1));
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, (char) 0);
        Assertions.assertArrayEquals(new char[10], array);
    }

    @Test
    void fillIntArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        int[] expected = new int[10];
        java.util.Arrays.fill(expected, Integer.MAX_VALUE - 1);

        int[] array = new int[10];
        Arrays.fill(array, Integer.MAX_VALUE - 1);
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, 0);
        Assertions.assertArrayEquals(new int[10], array);
    }

    @Test
    void fillLongArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        long[] expected = new long[10];
        java.util.Arrays.fill(expected, Long.MAX_VALUE - 1);

        long[] array = new long[10];
        Arrays.fill(array, Long.MAX_VALUE - 1);
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, 0);
        Assertions.assertArrayEquals(new long[10], array);
    }

    @Test
    void fillFloatArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        float[] expected = new float[10];
        java.util.Arrays.fill(expected, Float.MAX_VALUE - 1);

        float[] array = new float[10];
        Arrays.fill(array, Float.MAX_VALUE - 1);
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, 0);
        Assertions.assertArrayEquals(new float[10], array);
    }

    @Test
    void fillDoubleArray() {
        if (JVMConstants.OPENJ9_RUNTIME) return; //OpenJ9 does not support this

        double[] expected = new double[10];
        java.util.Arrays.fill(expected, Double.MAX_VALUE - 1);

        double[] array = new double[10];
        Arrays.fill(array, Double.MAX_VALUE - 1);
        Assertions.assertArrayEquals(expected, array);

        Arrays.fill(array, 0);
        Assertions.assertArrayEquals(new double[10], array);
    }

}
