package net.lenni0451.reflect;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class contains some useful methods for working with arrays.
 */
@ParametersAreNonnullByDefault
public class Arrays {

    /**
     * Set the length of an array.<br>
     * It is recommended to only decrease the length of the array.<br>
     * Increasing the length of the array would overwrite the memory of the next object.
     *
     * @param array     The array
     * @param newLength The new length
     */
    public static void setLength(final Object array, final int newLength) {
        if (!array.getClass().isArray()) throw new IllegalArgumentException("Object is not an array");
        JavaBypass.UNSAFE.putInt(array, (long) Objects.OBJECT_HEADER_SIZE, newLength);
    }

    /**
     * Fill a byte array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final byte[] array, final byte value) {
        JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length, value);
    }

    /**
     * Fill a short array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final short[] array, final short value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 2L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 2; i += 2) {
                JavaBypass.UNSAFE.putShort(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

    /**
     * Fill a char array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final char[] array, final char value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 2L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 2; i += 2) {
                JavaBypass.UNSAFE.putChar(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

    /**
     * Fill an int array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final int[] array, final int value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 4L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 4; i += 4) {
                JavaBypass.UNSAFE.putInt(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

    /**
     * Fill a long array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final long[] array, final long value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 8L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 8; i += 8) {
                JavaBypass.UNSAFE.putLong(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

    /**
     * Fill a float array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final float[] array, final float value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 4L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 4; i += 4) {
                JavaBypass.UNSAFE.putFloat(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

    /**
     * Fill a double array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final double[] array, final double value) {
        if (value == 0) {
            JavaBypass.UNSAFE.setMemory(array, Objects.ARRAY_HEADER_SIZE, array.length * 8L, (byte) 0);
        } else {
            for (int i = 0; i < array.length * 8; i += 8) {
                JavaBypass.UNSAFE.putDouble(array, (long) Objects.ARRAY_HEADER_SIZE + i, value);
            }
        }
    }

}
