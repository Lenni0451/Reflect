package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.commons.unchecked.FieldInitializer;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

/**
 * This class contains some useful methods for working with modules.
 */
public class Modules {

    private static final Field moduleField = FieldInitializer
            .attempt(() -> Fields.getDeclaredField(Class.class, FIELD_Class_module))
            .require(() -> new FieldNotFoundException(FIELD_Class_module, Class.class.getName()));
    private static final Field everyoneModuleField = FieldInitializer
            .attempt(() -> Fields.getDeclaredField(Module.class, FIELD_Module_EVERYONE_MODULE))
            .require(() -> new FieldNotFoundException(FIELD_Module_EVERYONE_MODULE, Module.class.getName()));
    private static final MethodHandle implAddExportsOrOpens = FieldInitializer
            .attempt(() -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddExportsOrOpens, String.class, Module.class, boolean.class, boolean.class))
            .map(TRUSTED_LOOKUP::unreflect)
            .require(() -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddExportsOrOpens, String.class, Module.class, boolean.class, boolean.class));
    private static final MethodHandle implAddEnableNativeAccess = FieldInitializer
            .attempt(() -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddEnableNativeAccess))
            .map(TRUSTED_LOOKUP::unreflect)
            .require(() -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddEnableNativeAccess));
    private static final MethodHandle implAddEnableNativeAccessToAllUnnamed = FieldInitializer
            .attempt(() -> Methods.getDeclaredMethod(Module.class, METHOD_Module_implAddEnableNativeAccessToAllUnnamed))
            .map(TRUSTED_LOOKUP::unreflect)
            .require(() -> new MethodNotFoundException(Module.class.getName(), METHOD_Module_implAddEnableNativeAccessToAllUnnamed));

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
     * Open the entire boot module layer to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     */
    @SneakyThrows
    public static void openBootModule() {
        Module everyone = Fields.get(null, everyoneModuleField);
        for (Module module : ModuleLayer.boot().modules()) {
            for (String pkg : module.getPackages()) {
                implAddExportsOrOpens.invoke(module, pkg, everyone, true, true);
            }
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
