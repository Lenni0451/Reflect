package net.lenni0451.reflect;

import net.lenni0451.reflect.exceptions.ConstructorNotFoundException;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.utils.FieldInitializer.reqOptInit;

/**
 * This class contains some useful methods for working with enums.
 */
public class Enums {

    private static final Field enumVarsField = reqOptInit(
            OPENJ9_RUNTIME,
            () -> Fields.getDeclaredField(Class.class, FIELD_Class_EnumVars),
            () -> new FieldNotFoundException(Class.class.getName(), FIELD_Class_EnumVars)
    );
    private static final Field enumConstantsField = reqOptInit(
            !OPENJ9_RUNTIME,
            () -> Fields.getDeclaredField(Class.class, FIELD_Class_enumConstants),
            () -> new FieldNotFoundException(Class.class.getName(), FIELD_Class_enumConstants)
    );
    private static final Field enumConstantDirectoryField = reqOptInit(
            !OPENJ9_RUNTIME,
            () -> Fields.getDeclaredField(Class.class, FIELD_Class_enumConstantDirectory),
            () -> new FieldNotFoundException(Class.class.getName(), FIELD_Class_enumConstantDirectory)
    );

    /**
     * Create a new instance of an enum.<br>
     * The value will <b>not</b> be added to the enum class. Use {@link #addEnumInstance(Class, Enum)} for that.
     *
     * @param enumClass     The enum class
     * @param name          The name of the enum value
     * @param ordinal       The ordinal of the enum value
     * @param argumentTypes The argument types of the constructor
     * @param arguments     The arguments of the constructor
     * @param <T>           The enum type
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

        Constructor<T> constructor = Constructors.getDeclaredConstructor(enumClass, types);
        if (constructor == null) throw new ConstructorNotFoundException(enumClass.getSimpleName(), argumentTypes);
        return Constructors.invoke(constructor, args);
    }

    /**
     * Add a new enum value to an enum class.<br>
     * The enum value will be added to the enum class and the enum value cache will be cleared.<br>
     * The ordinal of the enum value will be ignored. The value will be added at the end of the enum.
     *
     * @param enumClass The enum class
     * @param enumValue The enum value to add
     * @param <T>       The enum type
     */
    public static <T extends Enum<T>> void addEnumInstance(final Class<T> enumClass, final T enumValue) {
        //Add the enum value to the enum class
        Field values = Fields.getDeclaredField(enumClass, FIELD_Enum_$VALUES);
        Object[] valuesArray = Fields.getObject(null, values);
        valuesArray = Arrays.copyOf(valuesArray, valuesArray.length + 1);
        valuesArray[valuesArray.length - 1] = enumValue;
        Fields.setObject(null, values, valuesArray);

        clearEnumCache(enumClass);
    }

    /**
     * Clear the enum value cache of an enum class.<br>
     * This will force the JVM to re-calculate the enum values when calling {@link Class#getEnumConstants()}.
     *
     * @param enumClass The enum class
     */
    public static void clearEnumCache(final Class<?> enumClass) {
        if (OPENJ9_RUNTIME) {
            Fields.setObject(enumClass, enumVarsField, null);
        } else {
            Fields.setObject(enumClass, enumConstantsField, null);
            Fields.setObject(enumClass, enumConstantDirectoryField, null);
        }
    }

    /**
     * Get an enum value by its name ignoring the case.<br>
     * This method will return null if the enum value doesn't exist.
     *
     * @param enumClass The enum class
     * @param name      The name of the enum value
     * @return The enum value or null if it doesn't exist
     */
    @Nullable
    public static Object valueOfIgnoreCase(final Class<?> enumClass, final String name) {
        for (Object constant : enumClass.getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constant;
            if (enumConstant.name().equalsIgnoreCase(name)) return constant;
        }
        return null;
    }

}
