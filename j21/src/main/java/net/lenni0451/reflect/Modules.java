package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

/**
 * This class contains some useful methods for working with modules.
 */
public class Modules {

    private static final Field moduleField = reqInit(
            () -> Fields.getDeclaredField(Class.class, FIELD_Class_module),
            () -> new FieldNotFoundException(FIELD_Class_module, Class.class.getName())
    );
    private static final Field everyoneModuleField = reqInit(
            () -> Fields.getDeclaredField(Module.class, FIELD_Module_EVERYONE_MODULE),
            () -> new FieldNotFoundException(FIELD_Module_EVERYONE_MODULE, Module.class.getName())
    );
    private static final MethodHandle implAddExportsOrOpens = reqInit(
            () -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddExportsOrOpens, String.class, Module.class, boolean.class, boolean.class),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddExportsOrOpens, String.class, Module.class, boolean.class, boolean.class)
    );
    private static final MethodHandle implAddEnableNativeAccess = reqInit(
            () -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddEnableNativeAccess),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddEnableNativeAccess)
    );
    private static final MethodHandle implAddEnableNativeAccessToAllUnnamed = reqInit(
            () -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddEnableNativeAccessToAllUnnamed),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddEnableNativeAccessToAllUnnamed)
    );

    /**
     * Copy the module from one class to another.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param from The class to copy the module from
     * @param to   The class to copy the module to
     */
    public static void copyModule(final Class<?> from, final Class<?> to) {
        Fields.copyObject(from, to, moduleField);
    }

    /**
     * Open a module of a class to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     */
    public static void openModule(final Class<?> clazz) {
        openModule(clazz, clazz.getPackage().getName());
    }

    /**
     * Open a package of a module to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     * @param pkg   The package to open
     */
    @SneakyThrows
    public static void openModule(final Class<?> clazz, final String pkg) {
        Module everyone = Fields.get(null, everyoneModuleField);
        implAddExportsOrOpens.invoke(clazz.getModule(), pkg, everyone, true, true);
    }

    /**
     * Open all packages of a module to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     */
    @SneakyThrows
    public static void openEntireModule(final Class<?> clazz) {
        Module everyone = Fields.get(null, everyoneModuleField);
        for (String pkg : clazz.getModule().getPackages()) {
            implAddExportsOrOpens.invoke(clazz.getModule(), pkg, everyone, true, true);
        }
    }

    /**
     * Enable native access for a module.<br>
     * This allows the usage of the foreign memory API without the need to add the JVM argument.
     *
     * @param clazz The class to enable native access for
     */
    @SneakyThrows
    public static void enableNativeAccess(final Class<?> clazz) {
        implAddEnableNativeAccess.invoke(clazz.getModule());
    }

    /**
     * Enable native access for all unnamed modules.<br>
     * This allows the usage of the foreign memory API without the need to add the JVM argument.
     */
    @SneakyThrows
    public static void enableNativeAccessToAllUnnamed() {
        implAddEnableNativeAccessToAllUnnamed.invoke();
    }

}
