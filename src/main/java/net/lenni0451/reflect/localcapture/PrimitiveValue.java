package net.lenni0451.reflect.localcapture;

/**
 * Represents a primitive value in the JVM.<br>
 * When getting stack frame information, the JVM returns all primitives wrapped without type information.<br>
 * The primitive wrapped in this class may be a {@code boolean}, {@code byte}, {@code short}, {@code char}, {@code int}, {@code long}, {@code float} or {@code double}.
 */
public class PrimitiveValue {

    private final int size;
    private final long value;

    public PrimitiveValue(final int size, final long value) {
        this.size = size;
        this.value = value;
    }

    /**
     * @return The size of the value in bytes
     */
    public int getSize() {
        return this.size;
    }

    /**
     * @return The value of the primitive
     */
    public long getValue() {
        return this.value;
    }

    /**
     * @return The value as a boolean
     */
    public boolean asBoolean() {
        return this.value != 0;
    }

    /**
     * @return The value as a byte
     */
    public byte asByte() {
        return (byte) this.value;
    }

    /**
     * @return The value as a short
     */
    public short asShort() {
        return (short) this.value;
    }

    /**
     * @return The value as a char
     */
    public char asChar() {
        return (char) this.value;
    }

    /**
     * @return The value as an int
     */
    public int asInt() {
        return (int) this.value;
    }

    /**
     * @return The value as a long
     */
    public long asLong() {
        return this.value;
    }

    /**
     * @return The value as a float
     */
    public float asFloat() {
        return Float.intBitsToFloat((int) this.value);
    }

    /**
     * @return The value as a double
     */
    public double asDouble() {
        return Double.longBitsToDouble(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

}
