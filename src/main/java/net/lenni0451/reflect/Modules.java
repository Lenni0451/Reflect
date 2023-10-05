package net.lenni0451.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static net.lenni0451.reflect.JVMConstants.*;

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
        Field moduleField = Fields.getDeclaredField(Class.class, "module");
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
    public static void openModule(final Class<?> clazz, final String pkg) {
        Field moduleField = Fields.getDeclaredField(Class.class, FIELD_Class_module);
        if (moduleField == null) return;
        Field everyoneModuleField = Fields.getDeclaredField(moduleField.getType(), FIELD_Module_EVERYONE_MODULE);
        Method implAddExportsOrOpens = Methods.getDeclaredMethod(moduleField.getType(), METHOD_Module_implAddExportsOrOpens, String.class, moduleField.getType(), boolean.class, boolean.class);

        Object everyone = Fields.get(null, everyoneModuleField);
        Object module = Fields.get(clazz, moduleField);
        Methods.invoke(module, implAddExportsOrOpens, pkg, everyone, true, true);
    }

    /**
     * Open all packages of a module to everyone.<br>
     * This allows the usage of jdk internal classes which are normally protected by restricted module access.<br>
     * In Java 8 this method does nothing.
     *
     * @param clazz The class to open the module of
     */
    public static void openEntireModule(final Class<?> clazz) {
        Field moduleField = Fields.getDeclaredField(Class.class, FIELD_Class_module);
        if (moduleField == null) return;
        Field everyoneModuleField = Fields.getDeclaredField(moduleField.getType(), FIELD_Module_EVERYONE_MODULE);
        Method implAddExportsOrOpens = Methods.getDeclaredMethod(moduleField.getType(), METHOD_Module_implAddExportsOrOpens, String.class, moduleField.getType(), boolean.class, boolean.class);
        Method getPackages = Methods.getDeclaredMethod(moduleField.getType(), METHOD_Module_getPackages);

        Object everyone = Fields.get(null, everyoneModuleField);
        Object module = Fields.get(clazz, moduleField);
        Set<String> packages = Methods.invoke(module, getPackages);
        for (String pkg : packages) Methods.invoke(module, implAddExportsOrOpens, pkg, everyone, true, true);
    }

}
