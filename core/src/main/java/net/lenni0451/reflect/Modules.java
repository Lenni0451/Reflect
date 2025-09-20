package net.lenni0451.reflect;

import lombok.SneakyThrows;

/**
 * This class contains some useful methods for working with modules.
 */
public class Modules {

    /**
     * Copy the module from one class to another.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param from The class to copy the module from
     * @param to   The class to copy the module to
     */
    public static void copyModule(final Class<?> from, final Class<?> to) {
        //Nothing to do in Java 8
        //Check out the Java 9+ version
    }

    /**
     * Open a module of a class to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     */
    public static void openModule(final Class<?> clazz) {
        //Nothing to do in Java 8
        //Check out the Java 9+ version
    }

    /**
     * Open a package of a module to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     * @param pkg   The package to open
     */
    public static void openModule(final Class<?> clazz, final String pkg) {
        //Nothing to do in Java 8
        //Check out the Java 9+ version
    }

    /**
     * Open all packages of a module to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     */
    public static void openEntireModule(final Class<?> clazz) {
        //Nothing to do in Java 8
        //Check out the Java 9+ version
    }

    /**
     * Enable native access for a module.<br>
     * This allows the usage of the foreign memory API without the need to add the JVM argument.
     *
     * @param clazz The class to enable native access for
     */
    @SneakyThrows
    public static void enableNativeAccess(final Class<?> clazz) {
        //Nothing to do in Java 8
        //Check out the Java 21+ version
    }

    /**
     * Enable native access for all unnamed modules.<br>
     * This allows the usage of the foreign memory API without the need to add the JVM argument.
     */
    @SneakyThrows
    public static void enableNativeAccessToAllUnnamed() {
        //Nothing to do in Java 8
        //Check out the Java 21+ version
    }

}
