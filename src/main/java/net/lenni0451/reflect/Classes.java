package net.lenni0451.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.lenni0451.reflect.JavaBypass.UNSAFE;

public class Classes {

    /**
     * Get all declared classes of a class
     *
     * @param clazz The class to get the declared classes of
     * @return An array of all declared classes of the class
     */
    public static Class<?>[] getDeclaredClasses(final Class<?> clazz) {
        try {
            Method getDeclaredClasses0 = Class.class.getDeclaredMethod("getDeclaredClasses0");
            return Methods.invoke(clazz, getDeclaredClasses0, false);
        } catch (Throwable ignored) {
        }
        return new Class<?>[0];
    }

    /**
     * Get a declared class by its name
     *
     * @param clazz      The class to get the declared class of
     * @param simpleName The name of the class
     * @return The declared class or null if not found
     */
    public static Class<?> getDeclaredClass(final Class<?> clazz, final String simpleName) {
        for (Class<?> c : getDeclaredClasses(clazz)) if (c.getSimpleName().equals(simpleName)) return c;
        return null;
    }


    /**
     * Copy the module from one class to another<br>
     * This bypasses all reflection restrictions in place
     *
     * @param from The class to copy the module from
     * @param to   The class to copy the module to
     */
    public static void copyModule(final Class<?> from, final Class<?> to) {
        Field moduleField = Fields.getDeclaredField(Class.class, "module");
        if (moduleField == null) return;
        Fields.copyObject(from, to, moduleField);
    }

    /**
     * Open a module of a class to everyone<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access
     *
     * @param clazz The class to open the module of
     */
    public static void openModule(final Class<?> clazz) {
        Field moduleField = Fields.getDeclaredField(Class.class, "module");
        if (moduleField == null) return;
        Field everyoneModuleField = Fields.getDeclaredField(moduleField.getType(), "EVERYONE_MODULE");
        Method implAddExportsOrOpens = Methods.getDeclaredMethod(moduleField.getType(), "implAddExportsOrOpens", String.class, moduleField.getType(), boolean.class, boolean.class);

        Object everyone = Fields.get(null, everyoneModuleField);
        Object module = Fields.get(clazz, moduleField);
        Methods.invoke(module, implAddExportsOrOpens, Object.class.getPackage().getName(), everyone, true, true);
    }


    /**
     * Get a class by its name<br>
     * Basically a wrapper for {@link Class#forName(String)} but without direct exceptions
     *
     * @param name The name of the class
     * @return The class
     */
    public static Class<?> forName(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

    /**
     * Get a class by its name from a given class loader
     *
     * @param loader The class loader to get the class from
     * @param name   The name of the class
     * @return The class
     */
    public static Class<?> forName(final ClassLoader loader, final String name) {
        return forName(loader, name, true);
    }

    /**
     * Get a class by its name from a given class loader<br>
     * Basically a wrapper for {@link Class#forName(String, boolean, ClassLoader)} but without direct exceptions
     *
     * @param loader     The class loader to get the class from
     * @param name       The name of the class
     * @param initialize Whether to initialize the class
     * @return The class
     */
    public static Class<?> forName(final ClassLoader loader, final String name, final boolean initialize) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException e) {
            UNSAFE.throwException(e);
        }
        return null;
    }

}
