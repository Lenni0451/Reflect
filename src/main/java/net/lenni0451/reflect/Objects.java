package net.lenni0451.reflect;

import java.lang.reflect.Array;

import static net.lenni0451.reflect.JavaBypass.UNSAFE;

/**
 * This class contains some methods to do unsafe operations.
 */
public class Objects {

    private static final String INVALID_OOP_SIZE = "OOP size is not 4 or 8";
    private static final ThreadLocal<Object[]> OBJECT_ARRAY_CACHE = ThreadLocal.withInitial(() -> new Object[1]);
    public static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(Object[].class);
    public static final int ARRAY_INDEX_SCALE = UNSAFE.arrayIndexScale(Object[].class);
    public static final int ADDRESS_SIZE = UNSAFE.addressSize();
    public static final int OOP_SIZE = CompressedOopsClass.getOopSize();
    public static final int OBJECT_HEADER_SIZE = BooleanHeaderClass.getHeaderSize();
    public static final int ARRAY_HEADER_SIZE = OBJECT_HEADER_SIZE + 4;
    public static final boolean COMPRESSED_OOPS = ADDRESS_SIZE != OOP_SIZE;
    public static final long KLASS_OFFSET = Objects.OBJECT_HEADER_SIZE - Objects.OOP_SIZE;

    /**
     * Get the memory address of an object.
     *
     * @param o The object
     * @return The memory address
     */
    public static long toAddress(final Object o) {
        Object[] array = OBJECT_ARRAY_CACHE.get();
        array[0] = o;
        long address;
        if (OOP_SIZE == 4) address = UNSAFE.getInt(array, ARRAY_BASE_OFFSET) & 0xFFFFFFFFL;
        else if (OOP_SIZE == 8) address = UNSAFE.getLong(array, ARRAY_BASE_OFFSET);
        else throw new IllegalStateException(INVALID_OOP_SIZE);
        array[0] = null;
        return address;
    }

    /**
     * Get the object at the given memory address.
     *
     * @param address The memory address
     * @param <T>     The type of the object
     * @return The object
     */
    public static <T> T fromAddress(final long address) {
        Object[] array = OBJECT_ARRAY_CACHE.get();
        if (OOP_SIZE == 4) UNSAFE.putInt(array, ARRAY_BASE_OFFSET, (int) address);
        else if (OOP_SIZE == 8) UNSAFE.putLong(array, ARRAY_BASE_OFFSET, address);
        else throw new IllegalStateException(INVALID_OOP_SIZE);
        Object o = array[0];
        array[0] = null;
        return (T) o;
    }

    /**
     * Copy the memory from one object to another.
     *
     * @param from The source object
     * @param to   The target object
     * @param size The size of the memory to copy
     */
    public static void copyMemory(final Object from, final Object to, final long size) {
        copyMemory(from, 0, to, 0, size);
    }

    /**
     * Copy the memory from one object to another with the given offsets.
     *
     * @param from       The source object
     * @param fromOffset The offset in the source object
     * @param to         The target object
     * @param toOffset   The offset in the target object
     * @param size       The size of the memory to copy
     */
    public static void copyMemory(final Object from, final long fromOffset, final Object to, final long toOffset, final long size) {
        UNSAFE.copyMemory(toAddress(from) + fromOffset, toAddress(to) + toOffset, size);
    }

    /**
     * Get the class pointer of the given class.
     *
     * @param clazz The class
     * @return The class pointer
     */
    public static long getKlass(final Class<?> clazz) {
        try {
            if (clazz.isArray()) return getKlass(Array.newInstance(clazz.getComponentType(), 0));
            else return getKlass(UNSAFE.allocateInstance(clazz));
        } catch (Throwable t) {
            UNSAFE.throwException(t);
            return 0;
        }
    }

    /**
     * Get the class pointer of the given object.
     *
     * @param o The object
     * @return The class pointer
     */
    public static long getKlass(final Object o) {
        if (OOP_SIZE == 4) return UNSAFE.getInt(o, KLASS_OFFSET) & 0xFFFFFFFFL;
        else if (OOP_SIZE == 8) return UNSAFE.getLong(o, KLASS_OFFSET);
        else throw new IllegalStateException(INVALID_OOP_SIZE);
    }

    /**
     * Cast the given object to the given class.
     *
     * @param o      The object
     * @param target The target class
     * @param <T>    The type of the object
     * @return The casted object
     */
    public static <T> T cast(final Object o, final Class<T> target) {
        return cast(o, getKlass(target));
    }

    /**
     * Cast the given object to the given object.
     *
     * @param o      The object
     * @param target The target object
     * @param <T>    The type of the object
     * @return The casted object
     */
    public static <T> T cast(final Object o, final Object target) {
        return cast(o, getKlass(target));
    }

    /**
     * Cast the given object to the given class pointer.
     *
     * @param o     The object
     * @param klass The class pointer
     * @param <T>   The type of the object
     * @return The casted object
     */
    public static <T> T cast(final Object o, final long klass) {
        if (JVMConstants.OPENJ9_RUNTIME) throw new UnsupportedOperationException("OpenJ9 is not supported");
        if (OOP_SIZE == 4) UNSAFE.putInt(o, KLASS_OFFSET, (int) klass);
        else if (OOP_SIZE == 8) UNSAFE.putLong(o, KLASS_OFFSET, klass);
        else throw new IllegalStateException(INVALID_OOP_SIZE);
        return (T) o;
    }


    private static class CompressedOopsClass {
        public Object o1;
        public Object o2;

        private static int getOopSize() {
            try {
                long o1 = UNSAFE.objectFieldOffset(Fields.getDeclaredField(CompressedOopsClass.class, "o1"));
                long o2 = UNSAFE.objectFieldOffset(Fields.getDeclaredField(CompressedOopsClass.class, "o2"));
                return (int) Math.abs(o2 - o1);
            } catch (Throwable t) {
                UNSAFE.throwException(t);
                return 0;
            }
        }
    }

    private static class BooleanHeaderClass {
        public boolean b;

        private static int getHeaderSize() {
            try {
                long b = UNSAFE.objectFieldOffset(Fields.getDeclaredField(BooleanHeaderClass.class, "b"));
                return (int) b;
            } catch (Throwable t) {
                UNSAFE.throwException(t);
                return 0;
            }
        }
    }

}
