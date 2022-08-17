package net.lenni0451.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.lenni0451.reflect.JavaBypass.UNSAFE;

public class Fields {

    /**
     * Get the offset of a field
     *
     * @param field The field to get the offset of
     * @return The offset of the field
     */
    public static long offset(final Field field) {
        if (Modifier.isStatic(field.getModifiers())) return UNSAFE.staticFieldOffset(field);
        else return UNSAFE.objectFieldOffset(field);
    }

    /**
     * Get the instance used for getting and setting the value of the field<br>
     * Only exists to make the below code smaller
     *
     * @param instance The instance or null if the field is static
     * @param field    The field
     * @return The instance if virtual, otherwise the declaring class of the field
     */
    public static Object instance(final Object instance, final Field field) {
        return Modifier.isStatic(field.getModifiers()) ? field.getDeclaringClass() : instance;
    }


    /**
     * Get all declared fields of a class
     *
     * @param clazz The class to get the fields of
     * @return An array of all declared fields of the class
     */
    public static Field[] getDeclaredFields(final Class<?> clazz) {
        try {
            Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            return Methods.invoke(clazz, getDeclaredFields0, false);
        } catch (Throwable ignored) {
        }
        return new Field[0];
    }

    /**
     * Get a declared field of a class by name
     *
     * @param clazz The class to get the field of
     * @param name  The name of the field
     * @return The field with the name or null if not found
     */
    public static Field getDeclaredField(final Class<?> clazz, final String name) {
        for (Field field : getDeclaredFields(clazz)) if (field.getName().equals(name)) return field;
        return null;
    }


    public static boolean getBoolean(final Object instance, final Field field) {
        return UNSAFE.getBoolean(instance(instance, field), offset(field));
    }

    public static void setBoolean(final Object instance, final Field field, final boolean value) {
        UNSAFE.putBoolean(instance(instance, field), offset(field), value);
    }

    public static void copyBoolean(final Object instance, final Object target, final Field field) {
        setBoolean(target, field, getBoolean(instance, field));
    }


    public static byte getByte(final Object instance, final Field field) {
        return UNSAFE.getByte(instance(instance, field), offset(field));
    }

    public static void setByte(final Object instance, final Field field, final byte value) {
        UNSAFE.putByte(instance(instance, field), offset(field), value);
    }

    public static void copyByte(final Object instance, final Object target, final Field field) {
        setByte(target, field, getByte(instance, field));
    }


    public static short getShort(final Object instance, final Field field) {
        return UNSAFE.getShort(instance(instance, field), offset(field));
    }

    public static void setShort(final Object instance, final Field field, final short value) {
        UNSAFE.putShort(instance(instance, field), offset(field), value);
    }

    public static void copyShort(final Object instance, final Object target, final Field field) {
        setShort(target, field, getShort(instance, field));
    }


    public static char getChar(final Object instance, final Field field) {
        return UNSAFE.getChar(instance(instance, field), offset(field));
    }

    public static void setChar(final Object instance, final Field field, final char value) {
        UNSAFE.putChar(instance(instance, field), offset(field), value);
    }

    public static void copyChar(final Object instance, final Object target, final Field field) {
        setChar(target, field, getChar(instance, field));
    }


    public static int getInt(final Object instance, final Field field) {
        return UNSAFE.getInt(instance(instance, field), offset(field));
    }

    public static void setInt(final Object instance, final Field field, final int value) {
        UNSAFE.putInt(instance(instance, field), offset(field), value);
    }

    public static void copyInt(final Object instance, final Object target, final Field field) {
        setInt(target, field, getInt(instance, field));
    }


    public static long getLong(final Object instance, final Field field) {
        return UNSAFE.getLong(instance(instance, field), offset(field));
    }

    public static void setLong(final Object instance, final Field field, final long value) {
        UNSAFE.putLong(instance(instance, field), offset(field), value);
    }

    public static void copyLong(final Object instance, final Object target, final Field field) {
        setLong(target, field, getLong(instance, field));
    }


    public static float getFloat(final Object instance, final Field field) {
        return UNSAFE.getFloat(instance(instance, field), offset(field));
    }

    public static void setFloat(final Object instance, final Field field, final float value) {
        UNSAFE.putFloat(instance(instance, field), offset(field), value);
    }

    public static void copyFloat(final Object instance, final Object target, final Field field) {
        setFloat(target, field, getFloat(instance, field));
    }


    public static double getDouble(final Object instance, final Field field) {
        return UNSAFE.getDouble(instance(instance, field), offset(field));
    }

    public static void setDouble(final Object instance, final Field field, final double value) {
        UNSAFE.putDouble(instance(instance, field), offset(field), value);
    }

    public static void copyDouble(final Object instance, final Object target, final Field field) {
        setDouble(target, field, getDouble(instance, field));
    }


    public static <T> T getObject(final Object instance, final Field field) {
        return (T) UNSAFE.getObject(instance(instance, field), offset(field));
    }

    public static void setObject(final Object instance, final Field field, final Object value) {
        UNSAFE.putObject(instance(instance, field), offset(field), value);
    }

    public static void copyObject(final Object instance, final Object target, final Field field) {
        setObject(target, field, getObject(instance, field));
    }


    /**
     * Get the value of a field<br>
     * This is a wrapper method for the above methods automatically selecting the correct one based on the field type
     *
     * @param instance The instance to get the field from or null if static
     * @param field    The field to get the value from
     * @return The value of the field
     */
    public static <T> T get(final Object instance, final Field field) {
        if (field.getType().equals(boolean.class)) return (T) Boolean.valueOf(getBoolean(instance, field));
        else if (field.getType().equals(byte.class)) return (T) Byte.valueOf(getByte(instance, field));
        else if (field.getType().equals(short.class)) return (T) Short.valueOf(getShort(instance, field));
        else if (field.getType().equals(char.class)) return (T) Character.valueOf(getChar(instance, field));
        else if (field.getType().equals(int.class)) return (T) Integer.valueOf(getInt(instance, field));
        else if (field.getType().equals(long.class)) return (T) Long.valueOf(getLong(instance, field));
        else if (field.getType().equals(float.class)) return (T) Float.valueOf(getFloat(instance, field));
        else if (field.getType().equals(double.class)) return (T) Double.valueOf(getDouble(instance, field));
        else return getObject(instance, field);
    }

    /**
     * Set the value of a field<br>
     * This is a wrapper method for the above methods automatically selecting the correct one based on the field type
     *
     * @param instance The instance to set the field on or null if static
     * @param field    The field to set the value on
     * @param value    The value to set the field to
     */
    public static <T> void set(final Object instance, final Field field, final T value) {
        if (field.getType().equals(boolean.class)) setBoolean(instance, field, (Boolean) value);
        else if (field.getType().equals(byte.class)) setByte(instance, field, (Byte) value);
        else if (field.getType().equals(short.class)) setShort(instance, field, (Short) value);
        else if (field.getType().equals(char.class)) setChar(instance, field, (Character) value);
        else if (field.getType().equals(int.class)) setInt(instance, field, (Integer) value);
        else if (field.getType().equals(long.class)) setLong(instance, field, (Long) value);
        else if (field.getType().equals(float.class)) setFloat(instance, field, (Float) value);
        else if (field.getType().equals(double.class)) setDouble(instance, field, (Double) value);
        else setObject(instance, field, value);
    }

    /**
     * Copy the value of a field<br>
     * This is a wrapper method for the above methods automatically selecting the correct one based on the field type
     *
     * @param instance The instance to copy the field from or null if static
     * @param target   The instance to copy the field to
     * @param field    The field to copy the value from
     */
    public static <T> void copy(final Object instance, final Object target, final Field field) {
        if (field.getType().equals(boolean.class)) copyBoolean(instance, target, field);
        else if (field.getType().equals(byte.class)) copyByte(instance, target, field);
        else if (field.getType().equals(short.class)) copyShort(instance, target, field);
        else if (field.getType().equals(char.class)) copyChar(instance, target, field);
        else if (field.getType().equals(int.class)) copyInt(instance, target, field);
        else if (field.getType().equals(long.class)) copyLong(instance, target, field);
        else if (field.getType().equals(float.class)) copyFloat(instance, target, field);
        else if (field.getType().equals(double.class)) copyDouble(instance, target, field);
        else copyObject(instance, target, field);
    }

}
