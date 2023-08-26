package net.lenni0451.reflect;

import net.lenni0451.reflect.stream.RStream;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.function.Supplier;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.JavaBypass.UNSAFE;

/**
 * This class contains some useful methods for working with class loaders.
 */
public class ClassLoaders {

    private static final Class<?> classOptionClass = Classes.byName(CLASS_MethodHandles_Lookup_ClassOption);
    private static final Method unsafeDefineAnonymousClass = Methods.getDeclaredMethod(Unsafe.class, METHOD_Unsafe_defineAnonymousClass, Class.class, byte[].class, Object[].class);
    private static final Method lookupDefineHiddenClass = ((Supplier<Method>) () -> {
        if (classOptionClass == null) return null;
        return Methods.getDeclaredMethod(MethodHandles.Lookup.class, METHOD_MethodHandles_Lookup_defineHiddenClass, byte[].class, boolean.class, Array.newInstance(classOptionClass, 0).getClass());
    }).get();

    /**
     * Add a URL to the system classpath.
     *
     * @param url The URL to add
     * @throws IllegalStateException If the url class path could not be found
     */
    public static void addToSystemClassPath(final URL url) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Field ucpField = Fields.getDeclaredField(systemClassLoader.getClass(), FIELD_URLClassLoader_ucp);
        if (ucpField == null) ucpField = Fields.getDeclaredField(systemClassLoader.getClass().getSuperclass(), FIELD_URLClassLoader_ucp);
        if (ucpField == null) throw new IllegalStateException("Unable to find URLClassPath field of system classloader");
        Object urlClassPath = Fields.getObject(systemClassLoader, ucpField);
        Method addURLMethod = Methods.getDeclaredMethod(ucpField.getType(), METHOD_URLClassPath_addURL, URL.class);
        Methods.invoke(urlClassPath, addURLMethod, url);
    }

    /**
     * Get all URLs of the system classpath.
     *
     * @return The system classpath
     * @throws IllegalStateException If the url class path could not be found
     */
    public static URL[] getSystemClassPath() {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Field ucpField = Fields.getDeclaredField(systemClassLoader.getClass(), FIELD_URLClassLoader_ucp);
        if (ucpField == null) ucpField = Fields.getDeclaredField(systemClassLoader.getClass().getSuperclass(), FIELD_URLClassLoader_ucp);
        if (ucpField == null) throw new IllegalStateException("Unable to find URLClassPath field of system classloader");
        Object urlClassPath = Fields.getObject(systemClassLoader, ucpField);
        Method getURLsMethod = Methods.getDeclaredMethod(urlClassPath.getClass(), METHOD_URLClassPath_getURLs);
        return Methods.invoke(urlClassPath, getURLsMethod);
    }

    /**
     * Load a URL into the context class loader and move it to the front of the classpath.<br>
     * This allows overriding classes in the classpath with classes from an external jar.
     *
     * @param url The URL to load
     * @throws IllegalStateException If the url class path could not be found or the URL could not be found in the classpath
     */
    public static void loadToFront(final URL url) {
        //First add the URL into the classpath
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object ucp;
        try {
            ucp = RStream.of(classLoader).withSuper().fields().by("ucp").get();
            RStream.of(ucp).methods().by("addURL", URL.class).invokeArgs(url);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to find URLClassPath of classloader", t);
        }

        //Move the URL to the front of the classpath, so it gets loaded first
        MOVE_URL_UP:
        {
            List<URL> path = RStream.of(ucp).fields().by("path").get();
            for (int i = 0; i < path.size(); i++) {
                if (url.equals(path.get(i))) {
                    path.add(0, path.remove(i));
                    break MOVE_URL_UP;
                }
            }
            throw new IllegalStateException("Unable to find URL in classpath");
        }

        //Force the ClassLoader to load all URLs in the classpath
        URL nonExistentFile;
        do {
            //A deadlock could occur here theoretically, but it is very impossible
            nonExistentFile = classLoader.getResource("THIS_FILE_SHOULD_NEVER_EXIST_" + System.nanoTime());
        } while (nonExistentFile != null);

        //Move the loader for that URL to the front of the list
        Class<?> jarLoaderClass = Classes.forName(ucp.getClass().getName() + "$JarLoader");
        List<Object> loaders = RStream.of(ucp).fields().by("loaders").get();
        for (Object loader : loaders) {
            if (jarLoaderClass.equals(loader.getClass())) {
                URL loaderUrl = RStream.of(loader).fields().filter(URL.class).by(0).get();
                if (url.equals(loaderUrl)) {
                    loaders.add(0, loaders.remove(loaders.size() - 1));
                    break;
                }
            }
        }
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
        Method method = Methods.getDeclaredMethod(ClassLoader.class, METHOD_ClassLoader_defineClass, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
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
     * @throws IllegalArgumentException If a flag is unknown
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
