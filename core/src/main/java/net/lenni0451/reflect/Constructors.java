package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.ThrowingSupplier.getFirst;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

/**
 * This class contains some useful methods for working with constructors.
 */
public class Constructors {

    private static final MethodHandle getDeclaredConstructors0 = reqInit(
            () -> {
                if (JVMConstants.OPENJ9_RUNTIME) return Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredConstructors0);
                else return Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredConstructors0, boolean.class);
            },
            TRUSTED_LOOKUP::unreflect, () -> new MethodNotFoundException(Class.class.getName(), METHOD_Class_getDeclaredConstructors0, JVMConstants.OPENJ9_RUNTIME ? "" : "boolean")
    );
    private static final Class<?> MemberName = Classes.forName(CLASS_MemberName);
    private static final Class<?> DirectMethodHandle_Constructor = Classes.forName(CLASS_DirectMethodHandle_Constructor);
    private static final Class<?> MethodHandleNatives_Constants = Classes.forName(CLASS_MethodHandleNatives_Constants);
    private static final MethodHandle getInitMethod = reqInit(
            () -> TRUSTED_LOOKUP.findGetter(DirectMethodHandle_Constructor, "initMethod", MemberName),
            handle -> handle.asType(MethodType.methodType(Object.class, MethodHandle.class)),
            () -> new FieldNotFoundException(DirectMethodHandle_Constructor.getName(), "initMethod")
    );
    private static final MethodHandle getFlags = reqInit(
            () -> TRUSTED_LOOKUP.findGetter(MemberName, FIELD_MemberName_flags, int.class),
            handle -> handle.asType(MethodType.methodType(int.class, Object.class)),
            () -> new FieldNotFoundException(MemberName.getName(), FIELD_MemberName_flags)
    );
    private static final MethodHandle setFlags = reqInit(
            () -> TRUSTED_LOOKUP.findSetter(MemberName, FIELD_MemberName_flags, int.class),
            handle -> handle.asType(MethodType.methodType(void.class, Object.class, int.class)),
            () -> new FieldNotFoundException(MemberName.getName(), FIELD_MemberName_flags)
    );
    private static final int MN_IS_METHOD = reqInit(
            () -> TRUSTED_LOOKUP.findStaticGetter(MethodHandleNatives_Constants, FIELD_MethodHandleNatives_Constants_MN_IS_METHOD, int.class),
            handle -> (int) handle.invokeExact(),
            () -> new FieldNotFoundException(MethodHandleNatives_Constants.getName(), FIELD_MethodHandleNatives_Constants_MN_IS_METHOD)
    );
    private static final int MN_IS_CONSTRUCTOR = reqInit(
            () -> TRUSTED_LOOKUP.findStaticGetter(MethodHandleNatives_Constants, FIELD_MethodHandleNatives_Constants_MN_IS_CONSTRUCTOR, int.class),
            handle -> (int) handle.invokeExact(),
            () -> new FieldNotFoundException(MethodHandleNatives_Constants.getName(), FIELD_MethodHandleNatives_Constants_MN_IS_CONSTRUCTOR)
    );
    private static final MethodHandle getDirectMethod = reqInit(
            getFirst(
                    () -> MethodHandles.insertArguments(TRUSTED_LOOKUP.findVirtual(MethodHandles.Lookup.class, METHOD_MethodHandles_Lookup_getDirectMethod, MethodType.methodType(MethodHandle.class, byte.class, Class.class, MemberName, Class.class))
                            .asType(MethodType.methodType(MethodHandle.class, MethodHandles.Lookup.class, byte.class, Class.class, Object.class, Class.class)), 4, MethodHandles.Lookup.class),
                    () -> MethodHandles.insertArguments(TRUSTED_LOOKUP.findVirtual(MethodHandles.Lookup.class, METHOD_MethodHandles_Lookup_getDirectMethod, MethodType.methodType(MethodHandle.class, byte.class, Class.class, MemberName, MethodHandles.Lookup.class))
                            .asType(MethodType.methodType(MethodHandle.class, MethodHandles.Lookup.class, byte.class, Class.class, Object.class, MethodHandles.Lookup.class)), 4, TRUSTED_LOOKUP)
            ),
            () -> new MethodNotFoundException(MethodHandles.Lookup.class.getName(), METHOD_MethodHandles_Lookup_getDirectMethod, "byte", Class.class.getName(), MemberName.getName(), MethodHandles.Lookup.class.getName())
    );

    /**
     * Get all declared constructors of a class.<br>
     * The reflection filter of the class will be ignored.<br>
     * An empty array will be returned if the method could not be invoked.
     *
     * @param clazz The class to get the constructors from
     * @param <T>   The type of the class
     * @return An array of all declared constructors of the class
     * @throws MethodNotFoundException If the {@link Class} internal {@code getDeclaredConstructors0} method could not be found
     */
    @SneakyThrows
    public static <T> Constructor<T>[] getDeclaredConstructors(final Class<T> clazz) {
        if (JVMConstants.OPENJ9_RUNTIME) return (Constructor<T>[]) getDeclaredConstructors0.invokeExact(clazz);
        else return (Constructor<T>[]) getDeclaredConstructors0.invokeExact(clazz, false);
    }

    /**
     * Get a declared constructor of a class by its parameter types.<br>
     * The reflection filter of the class will be ignored.
     *
     * @param clazz          The class to get the constructor from
     * @param parameterTypes The parameter types of the constructor
     * @param <T>            The type of the class
     * @return The constructor or null if it doesn't exist
     */
    @Nullable
    public static <T> Constructor<T> getDeclaredConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        for (Constructor<T> constructor : getDeclaredConstructors(clazz)) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) return constructor;
        }
        return null;
    }


    /**
     * Invoke a constructor without any checks.<br>
     * The constructor does not have to be accessible.
     *
     * @param constructor The constructor to invoke
     * @param args        The arguments to pass to the constructor
     * @param <T>         The type of the class
     * @return The instance of the class
     * @throws RuntimeException If the constructor could not be invoked
     */
    public static <T> T invoke(final Constructor<T> constructor, final Object... args) {
        try {
            return (T) TRUSTED_LOOKUP.unreflectConstructor(constructor).asSpreader(Object[].class, args.length).invoke(args);
        } catch (Throwable t) {
            throw new ConstructorInvocationException(constructor).cause(t);
        }
    }

    /**
     * Make a {@link MethodHandle} pointing to a constructor invokable.<br>
     * This allows you to invoke the constructor multiple times and for objects that are already initialized.
     *
     * @param handle The constructor method handle
     * @return The invokable method handle
     */
    @SneakyThrows
    public static MethodHandle makeInvokable(final MethodHandle handle) {
        if (!DirectMethodHandle_Constructor.isInstance(handle)) throw new IllegalArgumentException("The method handle must be a DirectMethodHandle$Constructor");
        Object memberName = getInitMethod.invokeExact(handle);
        int flags = (int) getFlags.invokeExact(memberName);
        flags &= ~MN_IS_CONSTRUCTOR;
        flags |= MN_IS_METHOD;
        setFlags.invokeExact(memberName, flags);
        return (MethodHandle) getDirectMethod.invokeExact(TRUSTED_LOOKUP, (byte) 5, handle.type().returnType(), memberName);
    }

}
