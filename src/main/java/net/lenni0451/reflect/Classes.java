package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.accessor.UnsafeAccess;
import net.lenni0451.reflect.exceptions.MethodInvocationException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static net.lenni0451.reflect.JVMConstants.METHOD_Class_getDeclaredClasses0;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.optInit;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

/**
 * This class contains some useful methods for working with classes.
 */
public class Classes {

    private static final MethodHandle getDeclaredClasses0 = reqInit(
            () -> Methods.getDeclaredMethod(Class.class, METHOD_Class_getDeclaredClasses0),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodInvocationException(Class.class.getName(), METHOD_Class_getDeclaredClasses0)
    );
    private static final MethodHandle ensureInitialized = optInit(
            () -> Methods.getDeclaredMethod(MethodHandles.Lookup.class, "ensureInitialized", Class.class),
            TRUSTED_LOOKUP::unreflect
    );

    /**
     * Get all declared classes of a class.<br>
     * An empty array will be returned if the method could not be invoked.
     *
     * @param clazz The class to get the declared classes from
     * @return An array of all declared classes of the class
     * @throws MethodNotFoundException If the {@link Class} internal {@code getDeclaredClasses0} method could not be found
     */
    @SneakyThrows
    public static Class<?>[] getDeclaredClasses(final Class<?> clazz) {
        return (Class<?>[]) getDeclaredClasses0.invokeExact(clazz);
    }

    /**
     * Get a declared class of a class by its simple name.
     *
     * @param clazz      The class to get the declared class from
     * @param simpleName The simple name of the class
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> getDeclaredClass(final Class<?> clazz, final String simpleName) {
        for (Class<?> c : getDeclaredClasses(clazz)) {
            if (c.getSimpleName().equals(simpleName)) return c;
        }
        return null;
    }

    /**
     * Ensure that a class is initialized.<br>
     * Thrown exceptions will be ignored.
     *
     * @param clazz The class to initialize
     */
    public static void ensureInitialized(Class<?> clazz) {
        try { //Try using unsafe (deprecated since Java 15, available in internal unsafe)
            UnsafeAccess.ensureClassInitialized(clazz);
        } catch (Throwable ignored) {
        }
        try { //Try using trusted lookup
            if (ensureInitialized != null) {
                ensureInitialized.invokeExact(TRUSTED_LOOKUP.in(clazz), clazz);
                return;
            }
        } catch (Throwable ignored) {
        }
        try { //Fallback to forName()
            forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (Throwable ignored) {
        }
    }


    /**
     * Get a class by its name.<br>
     * A wrapper for {@link Class#forName(String)} returning null instead of throwing an exception.
     *
     * @param name The name of the class
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Get a class by its name.<br>
     * The {@code loader} parameter is set to the caller class loader.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name, final boolean initialize) {
        return byName(name, initialize, getCallerClass(1).getClassLoader());
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * The {@code initialize} parameter is set to true.
     *
     * @param name   The name of the class
     * @param loader The class loader to get the class from
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name, final ClassLoader loader) {
        return byName(name, true, loader);
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * A wrapper for {@link Class#forName(String, boolean, ClassLoader)} returning null instead of throwing an exception.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loader     The class loader to get the class from
     * @return The class or null if it doesn't exist
     */
    @Nullable
    public static Class<?> byName(final String name, final boolean initialize, final ClassLoader loader) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Get a class by its name.<br>
     * A wrapper for {@link Class#forName(String)} sneaky throwing an exception.
     *
     * @param name The name of the class
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nonnull
    @SneakyThrows
    public static Class<?> forName(final String name) {
        return Class.forName(name);
    }

    /**
     * Get a class by its name from.<br>
     * The {@code loader} parameter is set to the caller class loader.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nonnull
    public static Class<?> forName(final String name, final boolean initialize) {
        return forName(name, initialize, getCallerClass(1).getClassLoader());
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * The {@code initialize} parameter is set to true.
     *
     * @param name   The name of the class
     * @param loader The class loader to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nonnull
    public static Class<?> forName(final String name, final ClassLoader loader) {
        return forName(name, true, loader);
    }

    /**
     * Get a class by its name from a given class loader.<br>
     * A wrapper for {@link Class#forName(String, boolean, ClassLoader)} sneaky throwing an exception.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loader     The class loader to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @Nonnull
    @SneakyThrows
    public static Class<?> forName(final String name, final boolean initialize, final ClassLoader loader) {
        return Class.forName(name, initialize, loader);
    }

    /**
     * Get a class by its name from the first class loader that can find it.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loaders    The class loaders to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @SneakyThrows
    public static Class<?> find(final String name, final boolean initialize, final Iterable<ClassLoader> loaders) {
        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(name, initialize, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Get a class by its name from the first class loader that can find it.
     *
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @param loaders    The class loaders to get the class from
     * @return The class or null if it doesn't exist
     * @throws ClassNotFoundException If the class could not be found
     */
    @SneakyThrows
    public static Class<?> find(final String name, final boolean initialize, final ClassLoader... loaders) {
        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(name, initialize, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Get the class of the caller.<br>
     * Depth 0 is the caller of this method, depth 1 is the caller of the caller, etc.<br>
     * If the depth is too high, {@code null} will be returned.
     *
     * @param depth The depth of the caller
     * @return The class of the caller
     */
    @SneakyThrows
    public static Class<?> getCallerClass(final int depth) {
        return Classes$MR.getCallerClass(depth);
    }

}
