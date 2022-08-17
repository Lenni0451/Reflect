package net.lenni0451.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Enums {

    /**
     * Create a new enum instance during runtime
     *
     * @param enumClass     The enum class
     * @param name          The name of the new enum value
     * @param ordinal       The ordinal of the new enum value
     * @param argumentTypes The argument types of the enum constructor
     * @param arguments     The arguments of the enum constructor
     * @return The new enum value
     */
    public static <T extends Enum<T>> T newInstance(final Class<T> enumClass, final String name, final int ordinal, final Class<?>[] argumentTypes, final Object[] arguments) {
        Class<?>[] types = new Class<?>[arguments.length + 2];
        types[0] = String.class;
        types[1] = int.class;
        System.arraycopy(argumentTypes, 0, types, 2, argumentTypes.length);

        Object[] args = new Object[arguments.length + 2];
        args[0] = name;
        args[1] = ordinal;
        System.arraycopy(arguments, 0, args, 2, arguments.length);

        return Constructors.invoke(Constructors.getDeclaredConstructor(enumClass, types), args);
    }

    /**
     * Add a enum value to an enum class
     *
     * @param enumClass The enum class
     * @param enumValue The enum value
     */
    public static <T extends Enum<T>> void addEnumInstance(final Class<T> enumClass, final T enumValue) {
        { //Add the enum value to the enum class
            Field values = Fields.getDeclaredField(enumClass, "$VALUES");
            Object[] valuesArray = Fields.getObject(null, values);
            valuesArray = Arrays.copyOf(valuesArray, valuesArray.length + 1);
            valuesArray[valuesArray.length - 1] = enumValue;
            Fields.setObject(null, values, valuesArray);
        }
        { //Clear the enum value cache of the enum class
            Fields.setObject(enumClass, Fields.getDeclaredField(Class.class, "enumConstants"), null);
            Fields.setObject(enumClass, Fields.getDeclaredField(Class.class, "enumConstantDirectory"), null);
        }
    }

}
