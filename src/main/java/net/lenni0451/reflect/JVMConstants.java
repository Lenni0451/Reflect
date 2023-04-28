package net.lenni0451.reflect;

public class JVMConstants {

    public static final String CLASS_InstrumentationImpl = keep("sun.instrument.InstrumentationImpl");
    public static final String CLASS_MethodHandles_Lookup_ClassOption = keep("java.lang.invoke.MethodHandles$Lookup$ClassOption");
    public static final String CLASS_INTERNAL_Unsafe = keep("jdk.internal.misc.Unsafe");
    public static final String CLASS_INTERNAL_Reflection = keep("jdk.internal.reflect.Reflection");
    public static final String CLASS_SUN_Reflection = keep("sun.reflect.Reflection");

    public static final String FIELD_MethodHandles_Lookup_IMPL_LOOKUP = keep("IMPL_LOOKUP");
    public static final String FIELD_URLClassLoader_ucp = keep("ucp");
    public static final String FIELD_Enum_$VALUES = keep("$VALUES");
    public static final String FIELD_Class_enumConstants = keep("enumConstants");
    public static final String FIELD_Class_enumConstantDirectory = keep("enumConstantDirectory");
    public static final String FIELD_Reflection_fieldFilterMap = keep("fieldFilterMap");
    public static final String FIELD_Reflection_methodFilterMap = keep("methodFilterMap");
    public static final String FIELD_Class_module = keep("module");
    public static final String FIELD_Module_EVERYONE_MODULE = keep("EVERYONE_MODULE");

    public static final String METHOD_Class_getDeclaredClasses0 = keep("getDeclaredClasses0");
    public static final String METHOD_Class_getDeclaredFields0 = keep("getDeclaredFields0");
    public static final String METHOD_Class_getDeclaredConstructors0 = keep("getDeclaredConstructors0");
    public static final String METHOD_Class_getDeclaredMethods0 = keep("getDeclaredMethods0");
    public static final String METHOD_InstrumentationImpl_loadAgent = keep("loadAgent");
    public static final String METHOD_Unsafe_defineAnonymousClass = keep("defineAnonymousClass");
    public static final String METHOD_MethodHandles_Lookup_defineHiddenClass = keep("defineHiddenClass");
    public static final String METHOD_URLClassPath_addURL = keep("addURL");
    public static final String METHOD_URLClassPath_getURLs = keep("getURLs");
    public static final String METHOD_ClassLoader_defineClass = keep("defineClass");
    public static final String METHOD_Module_implAddExportsOrOpens = keep("implAddExportsOrOpens");


    /**
     * Prevent the java compiler from inlining static final strings.
     *
     * @param s The string to prevent from inlining
     * @return The same string
     */
    private static String keep(final String s) {
        return s;
    }

}
