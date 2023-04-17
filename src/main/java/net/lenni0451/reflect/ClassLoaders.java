package net.lenni0451.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.function.Supplier;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.JavaBypass.UNSAFE;

/**
 * This class contains some useful methods for working with class loaders.
 */
public class ClassLoaders {

    private static final Class<?> classOptionClass = Classes.byName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
    private static Method unsafeDefineAnonymousClass = Methods.getDeclaredMethod(Unsafe.class, "defineAnonymousClass", Class.class, byte[].class, Object[].class);
    private static Method lookupDefineHiddenClass = ((Supplier<Method>) () -> {
        if (classOptionClass == null) return null;
        return Methods.getDeclaredMethod(MethodHandles.Lookup.class, "defineHiddenClass", byte[].class, boolean.class, Array.newInstance(classOptionClass, 0).getClass());
    }).get();

    /**
     * Add a URL to the system classpath.
     *
     * @param url The URL to add
     */
    public static void addToSystemClassPath(final URL url) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Field ucpField = Fields.getDeclaredField(systemClassLoader.getClass(), "ucp");
        if (ucpField == null) ucpField = Fields.getDeclaredField(systemClassLoader.getClass().getSuperclass(), "ucp");
        if (ucpField == null) throw new IllegalStateException("Unable to find URLClassPath field of system classloader");
        Object urlClassPath = Fields.getObject(systemClassLoader, ucpField);
        Method addURLMethod = Methods.getDeclaredMethod(ucpField.getType(), "addURL", URL.class);
        Methods.invoke(urlClassPath, addURLMethod, url);
    }

    /**
     * Get all URLs of the system classpath.
     *
     * @return The system classpath
     */
    public static URL[] getSystemClassPath() {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Field ucpField = Fields.getDeclaredField(systemClassLoader.getClass(), "ucp");
        if (ucpField == null) ucpField = Fields.getDeclaredField(systemClassLoader.getClass().getSuperclass(), "ucp");
        if (ucpField == null) throw new IllegalStateException("Unable to find URLClassPath field of system classloader");
        Object urlClassPath = Fields.getObject(systemClassLoader, ucpField);
        Method getURLsMethod = Methods.getDeclaredMethod(urlClassPath.getClass(), "getURLs");
        return Methods.invoke(urlClassPath, getURLsMethod);
    }


    /**
     * Define a class using the given class loader.
     *
     * @param classLoader The class loader to use
     * @param name        The name of the class
     * @param bytecode    The bytecode of the class
     * @return The defined class
     */
    public static Class<?> defineClass(final ClassLoader classLoader, final String name, final byte[] bytecode) {
        return defineClass(classLoader, name, bytecode, null);
    }

    /**
     * Define a class using the given class loader.
     *
     * @param classLoader      The class loader to use
     * @param name             The name of the class
     * @param bytecode         The bytecode of the class
     * @param protectionDomain The protection domain of the class
     * @return The defined class
     */
    public static Class<?> defineClass(final ClassLoader classLoader, final String name, final byte[] bytecode, final ProtectionDomain protectionDomain) {
        return defineClass(classLoader, name, bytecode, 0, bytecode.length, protectionDomain);
    }

    /**
     * Define a class using the given class loader.
     *
     * @param classLoader      The class loader to use
     * @param name             The name of the class
     * @param bytecode         The bytecode of the class
     * @param offset           The offset of the class in the bytecode
     * @param length           The length of the class in the bytecode
     * @param protectionDomain The protection domain of the class
     * @return The defined class
     */
    public static Class<?> defineClass(final ClassLoader classLoader, final String name, final byte[] bytecode, final int offset, final int length, final ProtectionDomain protectionDomain) {
        Method method = Methods.getDeclaredMethod(ClassLoader.class, "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
        return Methods.invoke(classLoader, method, name, bytecode, offset, length, protectionDomain);
    }

    /**
     * Define an anonymous class.<br>
     * In Java 15 and above the {@code MethodHandles.Lookup#defineHiddenClass} method is used. On older versions the <code>Unsafe#defineAnonymousClass</code> method is used.<br>
     * The flags are case-insensitive and only used on Java 15 and above.
     *
     * @param parent   The parent class
     * @param bytecode The bytecode of the class
     * @param flags    The flags to use
     * @return The defined class
     */
    public static Class<?> defineAnonymousClass(final Class<?> parent, final byte[] bytecode, final String... flags) {
        if (classOptionClass == null) {
            return Methods.invoke(UNSAFE, unsafeDefineAnonymousClass, parent, bytecode, new Object[0]);
        } else {
            Object classOptions = Array.newInstance(classOptionClass, flags.length);
            for (int i = 0; i < flags.length; i++) {
                String flag = flags[i];
                Object classOption = Enums.valueOfIgnoreCase(classOptionClass, flag);
                if (classOption == null) throw new IllegalArgumentException("Unknown class option: " + flag);
                Array.set(classOptions, i, classOption);
            }
            MethodHandles.Lookup lookup = Methods.invoke(TRUSTED_LOOKUP.in(parent), lookupDefineHiddenClass, bytecode, false, classOptions);
            return lookup.lookupClass();
        }
    }

}
