package net.lenni0451.reflect.bytecode;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

@ApiStatus.Experimental
public class BytecodeUtils {

    /**
     * Replace all dots with slashes.
     *
     * @param className The class name
     * @return The class name with replaced dots
     */
    public static String slash(final String className) {
        return className.replace('.', '/');
    }

    /**
     * Get the name of the class with slashes instead of dots.
     *
     * @param clazz The class
     * @return The class name with slashes
     */
    public static String slash(final Class<?> clazz) {
        return slash(clazz.getName());
    }

    /**
     * Replace all slashes with dots.
     *
     * @param className The class name
     * @return The class name with replaced slashes
     */
    public static String dot(final String className) {
        return className.replace('/', '.');
    }

    /**
     * Get a descriptor for the given class name.<br>
     * Dots in the class name will automatically be replaced with slashes.
     *
     * @param className The class name
     * @return The descriptor
     */
    public static String desc(final String className) {
        return "L" + slash(className) + ";";
    }

    /**
     * Get the descriptor for the given class.<br>
     * This also supports primitive types and arrays.
     *
     * @param clazz The class
     * @return The descriptor
     */
    public static String desc(final Class<?> clazz) {
        if (void.class.equals(clazz)) return "V";
        else if (boolean.class.equals(clazz)) return "Z";
        else if (byte.class.equals(clazz)) return "B";
        else if (short.class.equals(clazz)) return "S";
        else if (char.class.equals(clazz)) return "C";
        else if (int.class.equals(clazz)) return "I";
        else if (long.class.equals(clazz)) return "J";
        else if (float.class.equals(clazz)) return "F";
        else if (double.class.equals(clazz)) return "D";
        else if (clazz.isArray()) return "[" + desc(clazz.getComponentType());
        else return desc(clazz.getName());
    }

    /**
     * Get the descriptor for the given method.
     *
     * @param method The method
     * @return The descriptor
     */
    public static String desc(final Method method) {
        return mdesc(method.getReturnType(), method.getParameterTypes());
    }

    /**
     * Get the descriptor for the given parameter types and return type.
     *
     * @param parameterTypes The parameter types
     * @param returnType     The return type
     * @return The descriptor
     */
    public static String mdesc(final Class<?> returnType, final Class<?>... parameterTypes) {
        StringBuilder builder = new StringBuilder("(");
        for (Class<?> parameterType : parameterTypes) builder.append(desc(parameterType));
        builder.append(")").append(desc(returnType));
        return builder.toString();
    }

    /**
     * Get the fitting return opcode for the given type.
     *
     * @param clazz The type
     * @return The opcode name
     */
    public static String getLoadOpcode(final Class<?> clazz) {
        if (boolean.class.equals(clazz) || byte.class.equals(clazz) || char.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz)) return "ILOAD";
        if (long.class.equals(clazz)) return "LLOAD";
        if (float.class.equals(clazz)) return "FLOAD";
        if (double.class.equals(clazz)) return "DLOAD";
        return "ALOAD";
    }

    /**
     * Get the fitting return opcode for the given type.<br>
     * {@link Void} will return {@code RETURN}.
     *
     * @param clazz The type
     * @return The opcode name
     */
    public static String getReturnOpcode(final Class<?> clazz) {
        if (void.class.equals(clazz)) return "RETURN";
        if (boolean.class.equals(clazz) || byte.class.equals(clazz) || char.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz)) return "IRETURN";
        if (long.class.equals(clazz)) return "LRETURN";
        if (float.class.equals(clazz)) return "FRETURN";
        if (double.class.equals(clazz)) return "DRETURN";
        return "ARETURN";
    }

    /**
     * Get the stack size for the given type.
     *
     * @param clazz The type
     * @return The stack size
     */
    public static int getStackSize(final Class<?> clazz) {
        if (long.class.equals(clazz) || double.class.equals(clazz)) return 2;
        return 1;
    }

    public static Class<?> boxed(final Class<?> clazz) {
        if (clazz == void.class) return Void.class;
        if (clazz == boolean.class) return Boolean.class;
        if (clazz == byte.class) return Byte.class;
        if (clazz == short.class) return Short.class;
        if (clazz == char.class) return Character.class;
        if (clazz == int.class) return Integer.class;
        if (clazz == long.class) return Long.class;
        if (clazz == float.class) return Float.class;
        if (clazz == double.class) return Double.class;
        return clazz;
    }

    public static Class<?> unboxed(final Class<?> clazz) {
        if (clazz == Void.class) return void.class;
        if (clazz == Boolean.class) return boolean.class;
        if (clazz == Byte.class) return byte.class;
        if (clazz == Short.class) return short.class;
        if (clazz == Character.class) return char.class;
        if (clazz == Integer.class) return int.class;
        if (clazz == Long.class) return long.class;
        if (clazz == Float.class) return float.class;
        if (clazz == Double.class) return double.class;
        return clazz;
    }

}
