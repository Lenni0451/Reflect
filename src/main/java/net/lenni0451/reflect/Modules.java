package net.lenni0451.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Modules {

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

}
