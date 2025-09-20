package net.lenni0451.reflect;

import net.lenni0451.reflect.accessor.UnsafeAccess;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class contains some useful methods for working with arrays.
 */
@ParametersAreNonnullByDefault
public class Arrays {

    private static final int ARRAY_LENGTH_OFFSET;

    static {
        //Find the memory offset of the length field in an array
        int lengthOffset = -1;
        byte[] test = new byte[123];
        for (int i = 0; i < Objects.BYTE_ARRAY_BASE_OFFSET; i++) {
            int arrayLength = test.length;
            if (UnsafeAccess.getInt(test, i) == arrayLength) {
                int newLength = arrayLength + 1;
                UnsafeAccess.putInt(test, i, newLength);
                if (test.length == newLength) {
                    lengthOffset = i;
                    break;
                }
                //Reset the memory if it's not the length field
                UnsafeAccess.putInt(test, i, arrayLength);
            }
        }
        ARRAY_LENGTH_OFFSET = lengthOffset;
    }

    /**
     * Set the length of an array.<br>
     * It is recommended to only decrease the length of the array.<br>
     * Increasing the length of the array would overwrite the memory of the next object.
     *
     * @param array     The array
     * @param newLength The new length
     */
    public static void setLength(final Object array, final int newLength) {
        if (ARRAY_LENGTH_OFFSET == -1) throw new UnsupportedOperationException("Could not find the array length field offset");
        if (!array.getClass().isArray()) throw new IllegalArgumentException("Object is not an array");
        UnsafeAccess.putInt(array, ARRAY_LENGTH_OFFSET, newLength);
    }

    /**
     * Fill a boolean array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final boolean[] array, final boolean value) {
        UnsafeAccess.setMemory(array, Objects.BOOLEAN_ARRAY_BASE_OFFSET, (long) array.length * Objects.BOOLEAN_ARRAY_INDEX_SCALE, (byte) (value ? 1 : 0));
    }

    /**
     * Fill a byte array with a value.<br>
     * This directly writes to the memory of the array and is therefore very fast.
     *
     * @param array The array
     * @param value The value to fill the array with
     */
    public static void fill(final byte[] array, final byte value) {
        UnsafeAccess.setMemory(array, Objects.BYTE_ARRAY_BASE_OFFSET, (long) array.length * Objects.BYTE_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.SHORT_ARRAY_BASE_OFFSET, (long) array.length * Objects.SHORT_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putShort(array, Objects.SHORT_ARRAY_BASE_OFFSET + (long) i * Objects.SHORT_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.CHAR_ARRAY_BASE_OFFSET, (long) array.length * Objects.CHAR_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putChar(array, Objects.CHAR_ARRAY_BASE_OFFSET + (long) i * Objects.CHAR_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.INT_ARRAY_BASE_OFFSET, (long) array.length * Objects.INT_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putInt(array, Objects.INT_ARRAY_BASE_OFFSET + (long) i * Objects.INT_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.LONG_ARRAY_BASE_OFFSET, (long) array.length * Objects.LONG_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putLong(array, Objects.LONG_ARRAY_BASE_OFFSET + (long) i * Objects.LONG_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.FLOAT_ARRAY_BASE_OFFSET, (long) array.length * Objects.FLOAT_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putFloat(array, Objects.FLOAT_ARRAY_BASE_OFFSET + (long) i * Objects.FLOAT_ARRAY_INDEX_SCALE, value);
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
            UnsafeAccess.setMemory(array, Objects.DOUBLE_ARRAY_BASE_OFFSET, (long) array.length * Objects.DOUBLE_ARRAY_INDEX_SCALE, (byte) 0);
        } else {
            for (int i = 0; i < array.length; i++) {
                UnsafeAccess.putDouble(array, Objects.DOUBLE_ARRAY_BASE_OFFSET + (long) i * Objects.DOUBLE_ARRAY_INDEX_SCALE, value);
            }
        }
    }

}
