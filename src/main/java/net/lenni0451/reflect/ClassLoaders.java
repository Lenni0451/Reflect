package net.lenni0451.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.JavaBypass.UNSAFE;

public class ClassLoaders {

    /**
     * Add a URL to the system classpath
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
     * Get all URLs of the system classpath
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
     * Define a class using the given class loader
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
     * Define a class using the given class loader
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
     * Define a class using the given class loader
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
     * Define an anonymous class<br>
     * This is java version independent but in java 17+ the parent class is unused
     *
     * @param parent   The parent class (unused in java 17+)
     * @param bytecode The bytecode of the class
     * @return The defined class
     */
    public static Class<?> defineAnonymousClass(final Class<?> parent, final byte[] bytecode) {
        Method unsafeDefineAnonymousClass = Methods.getDeclaredMethod(Unsafe.class, "defineAnonymousClass", Class.class, byte[].class, Object[].class);
        if (unsafeDefineAnonymousClass != null) return Methods.invoke(UNSAFE, unsafeDefineAnonymousClass, parent, bytecode, new Object[0]);

        Class<?> classOptionClass = Classes.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
        Object emptyClassOptionArray = Array.newInstance(classOptionClass, 0);
        Method lookupDefineHiddenClass = Methods.getDeclaredMethod(MethodHandles.Lookup.class, "defineHiddenClass", byte[].class, boolean.class, emptyClassOptionArray.getClass());
        MethodHandles.Lookup lookup = Methods.invoke(TRUSTED_LOOKUP, lookupDefineHiddenClass, bytecode, false, emptyClassOptionArray);
        return lookup.lookupClass();
    }

}
