package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Set;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.optInit;
import static net.lenni0451.reflect.utils.FieldInitializer.reqOptInit;

/**
 * This class contains some useful methods for working with modules.
 */
public class Modules {

    private static final Field moduleField = optInit(
            () -> Fields.getDeclaredField(Class.class, FIELD_Class_module)
    );
    private static final Field everyoneModuleField = reqOptInit(
            moduleField != null,
            () -> Fields.getDeclaredField(moduleField.getType(), FIELD_Module_EVERYONE_MODULE),
            () -> new FieldNotFoundException(FIELD_Module_EVERYONE_MODULE, moduleField.getType().getName())
    );
    private static final MethodHandle implAddExportsOrOpens = reqOptInit(
            moduleField != null,
            () -> Methods.getDeclaredMethod(moduleField.getType(), METHOD_Module_implAddExportsOrOpens, String.class, moduleField.getType(), boolean.class, boolean.class),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodNotFoundException(moduleField.getType().getName(), METHOD_Module_implAddExportsOrOpens, String.class, moduleField.getType(), boolean.class, boolean.class)
    );
    private static final MethodHandle getPackages = reqOptInit(
            moduleField != null,
            () -> Methods.getDeclaredMethod(moduleField.getType(), METHOD_Module_getPackages),
            TRUSTED_LOOKUP::unreflect,
            () -> new MethodNotFoundException(moduleField.getType().getName(), METHOD_Module_getPackages)
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
        if (moduleField == null) return;
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
        if (moduleField == null) return;
        Object everyone = Fields.get(null, everyoneModuleField);
        Object module = Fields.get(clazz, moduleField);
        implAddExportsOrOpens.invoke(module, pkg, everyone, true, true);
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
        if (moduleField == null) return;
        Object everyone = Fields.get(null, everyoneModuleField);
        Object module = Fields.get(clazz, moduleField);
        Set<String> packages = (Set<String>) getPackages.invoke(module);
        for (String pkg : packages) implAddExportsOrOpens.invoke(module, pkg, everyone, true, true);
    }

}
