package net.lenni0451.reflect;

import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;
import net.lenni0451.reflect.accessor.UnsafeAccess;
import net.lenni0451.reflect.exceptions.InvalidOOPSizeException;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;

import static net.lenni0451.reflect.utils.FieldInitializer.init;

/**
 * This class contains some methods to do unsafe operations.
 */
public class Objects {

    //Array constants
    public static final long BOOLEAN_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(boolean[].class);
    public static final int BOOLEAN_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(boolean[].class);
    public static final long BYTE_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(byte[].class);
    public static final int BYTE_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(byte[].class);
    public static final long SHORT_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(short[].class);
    public static final int SHORT_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(short[].class);
    public static final long CHAR_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(char[].class);
    public static final int CHAR_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(char[].class);
    public static final long INT_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(int[].class);
    public static final int INT_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(int[].class);
    public static final long LONG_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(long[].class);
    public static final int LONG_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(long[].class);
    public static final long FLOAT_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(float[].class);
    public static final int FLOAT_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(float[].class);
    public static final long DOUBLE_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(double[].class);
    public static final int DOUBLE_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(double[].class);
    public static final long OBJECT_ARRAY_BASE_OFFSET = UnsafeAccess.arrayBaseOffset(Object[].class);
    public static final int OBJECT_ARRAY_INDEX_SCALE = UnsafeAccess.arrayIndexScale(Object[].class);

    private static final ThreadLocal<Object[]> OBJECT_ARRAY_CACHE = ThreadLocal.withInitial(() -> new Object[1]);
    public static final int ADDRESS_SIZE = UnsafeAccess.addressSize();
    public static final int OOP_SIZE = CompressedOopsClass.getOopSize();
    public static final int OBJECT_HEADER_SIZE = BooleanHeaderClass.getHeaderSize();
    public static final int OBJECT_ALIGNMENT = init(() -> {
        if (!JVMConstants.OPENJ9_RUNTIME) { //OpenJ9 does not support this
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            if (mxBean != null) return Integer.parseInt(mxBean.getVMOption(JVMConstants.VM_OPTION_ObjectAlignmentInBytes).getValue());
        }
        return 8; //Default to 8 and hope for the best
    });
    public static final boolean COMPRESSED_OOPS = ADDRESS_SIZE != OOP_SIZE;
    public static final int COMPRESSED_OOP_SHIFT = init(() -> {
        int i = OBJECT_ALIGNMENT;
        int result = 0;
        while ((i >>= 1) != 0) result++;
        return result;
    });
    /**
     * The base address for compressed OOPs.<br>
     * The JVM can use different modes of memory layouts, so this can vary.
     * <table>
     *     <tr><th>Mode</th><th>Base</th><th>Shift</th><th>Condition</th></tr>
     *     <tr><td>None</td><td>0</td><td>0</td><td>The heap is not compressed,</td></tr>
     *     <tr><td>Zero-Based</td><td>0</td><td>3</td><td>The OS allocated the heap in the lower 32GB of virtual memory</td></tr>
     *     <tr><td>Disjoint Base</td><td>Non-Zero (e.g. 0x10000000)</td><td>3 (sometimes 0 if the heap is very small and fits in the first 4GB)</td><td>The heap is "floating" somewhere else, but the bits still align nicely</td></tr>
     *     <tr><td>Heap-Based (Scaled)</td><td>Non-zero arbitrary address</td><td>3</td><td></td></tr>
     * </table>
     * At the moment, this value is hardcoded to 0, because detecting the actual base address isn't trivial and would probably require native code.<br>
     * Since most JVMs use zero-based compressed OOPs (unless required otherwise), this should work fine for most use-cases.<br>
     * If you want to check the memory layout of your JVM, you can use the {@code -Xlog:gc+heap+coops}/{@code -Xlog:gc+heap+coops=debug,gc+init=info:stdout} option (OpenJDK 9+).
     */
    public static final long COMPRESSED_OOP_BASE = 0;
    public static final long KLASS_OFFSET = JVMConstants.OPENJ9_RUNTIME ? 0 : OBJECT_HEADER_SIZE - OOP_SIZE;

    /**
     * <b>Use {@link Objects#toJVMAddress(Object)}.</b>
     */
    @Deprecated
    public static long toAddress(final Object o) {
        return toJVMAddress(o);
    }

    /**
     * Get the jvm memory address of an object.
     *
     * @param o The object
     * @return The jvm memory address
     * @throws InvalidOOPSizeException If the OOP size is not 4 or 8
     */
    public static long toJVMAddress(final Object o) {
        Object[] array = OBJECT_ARRAY_CACHE.get();
        array[0] = o;
        long jvmAddress;
        if (OOP_SIZE == 4) jvmAddress = UnsafeAccess.getInt(array, OBJECT_ARRAY_BASE_OFFSET) & 0xFFFFFFFFL;
        else if (OOP_SIZE == 8) jvmAddress = UnsafeAccess.getLong(array, OBJECT_ARRAY_BASE_OFFSET);
        else throw new InvalidOOPSizeException();
        array[0] = null;
        return jvmAddress;
    }

    /**
     * Convert a native address to a jvm address.
     *
     * @param nativeAddress The native address
     * @return The jvm address
     */
    public static long toJVMAddress(final long nativeAddress) {
        if (COMPRESSED_OOPS) return (nativeAddress - COMPRESSED_OOP_BASE) >>> COMPRESSED_OOP_SHIFT;
        else return nativeAddress;
    }

    /**
     * Get the native address of an object.
     *
     * @param o The object
     * @return The native address
     * @throws IllegalStateException If the OOP size is not 4 or 8
     */
    public static long toNativeAddress(final Object o) {
        return toNativeAddress(toJVMAddress(o));
    }

    /**
     * Convert a jvm address to a native address.
     *
     * @param jvmAddress The jvm address
     * @return The native address
     */
    public static long toNativeAddress(final long jvmAddress) {
        if (COMPRESSED_OOPS) return COMPRESSED_OOP_BASE + (jvmAddress << COMPRESSED_OOP_SHIFT);
        else return jvmAddress;
    }

    /**
     * <b>Use {@link Objects#fromJVMAddress(long)}.</b>
     */
    @Deprecated
    public static <T> T fromAddress(final long jvmAddress) {
        return fromJVMAddress(jvmAddress);
    }

    /**
     * Get the object at the given jvm memory address.
     *
     * @param jvmAddress The jvm memory address
     * @param <T>        The type of the object
     * @return The object
     * @throws InvalidOOPSizeException If the OOP size is not 4 or 8
     */
    public static <T> T fromJVMAddress(final long jvmAddress) {
        Object[] array = OBJECT_ARRAY_CACHE.get();
        if (OOP_SIZE == 4) UnsafeAccess.putInt(array, OBJECT_ARRAY_BASE_OFFSET, (int) jvmAddress);
        else if (OOP_SIZE == 8) UnsafeAccess.putLong(array, OBJECT_ARRAY_BASE_OFFSET, jvmAddress);
        else throw new InvalidOOPSizeException();
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
        UnsafeAccess.copyMemory(toJVMAddress(from) + fromOffset, toJVMAddress(to) + toOffset, size);
    }

    /**
     * Get the class pointer of the given class.
     *
     * @param clazz The class
     * @return The class pointer
     * @throws InstantiationException If the class can not be allocated
     */
    @SneakyThrows
    public static long getKlass(final Class<?> clazz) {
        if (clazz.isArray()) return getKlass(Array.newInstance(clazz.getComponentType(), 0));
        else return getKlass(UnsafeAccess.allocateInstance(clazz));
    }

    /**
     * Get the class pointer of the given object.
     *
     * @param o The object
     * @return The class pointer
     * @throws InvalidOOPSizeException If the OOP size is not 4 or 8
     */
    public static long getKlass(final Object o) {
        if (OOP_SIZE == 4) return UnsafeAccess.getInt(o, KLASS_OFFSET) & 0xFFFFFFFFL;
        else if (OOP_SIZE == 8) return UnsafeAccess.getLong(o, KLASS_OFFSET);
        else throw new InvalidOOPSizeException();
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
     * @throws ClassCastException If the object can not be casted
     */
    public static <T> T cast(final Object o, final long klass) {
        if (OOP_SIZE == 4) UnsafeAccess.putInt(o, KLASS_OFFSET, (int) klass);
        else if (OOP_SIZE == 8) UnsafeAccess.putLong(o, KLASS_OFFSET, klass);
        else throw new InvalidOOPSizeException();
        return (T) o;
    }

    /**
     * Allocate an instance of the given class.
     *
     * @param clazz The class
     * @param <T>   The type of the instance
     * @return The allocated instance
     * @throws InstantiationException If the class can not be allocated
     * @throws ClassCastException     If the object does not match the given class
     */
    @SneakyThrows
    public static <T> T allocate(final Class<T> clazz) {
        return (T) UnsafeAccess.allocateInstance(clazz);
    }


    private static class CompressedOopsClass {
        public Object o1;
        public Object o2;

        private static int getOopSize() {
            long o1 = UnsafeAccess.objectFieldOffset(Fields.getDeclaredField(CompressedOopsClass.class, "o1"));
            long o2 = UnsafeAccess.objectFieldOffset(Fields.getDeclaredField(CompressedOopsClass.class, "o2"));
            return (int) Math.abs(o2 - o1);
        }
    }

    private static class BooleanHeaderClass {
        public boolean b;

        private static int getHeaderSize() {
            long b = UnsafeAccess.objectFieldOffset(Fields.getDeclaredField(BooleanHeaderClass.class, "b"));
            return (int) b;
        }
    }

}
