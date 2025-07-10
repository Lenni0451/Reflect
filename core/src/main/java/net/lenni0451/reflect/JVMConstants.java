package net.lenni0451.reflect;

public class JVMConstants {

    public static final boolean OPENJ9_RUNTIME = System.getProperty("java.vm.name").toLowerCase().contains("openj9");

    public static final String CLASS_InstrumentationImpl = calc("sun.instrument.InstrumentationImpl");
    public static final String CLASS_MethodHandles_Lookup_ClassOption = calc("java.lang.invoke.MethodHandles$Lookup$ClassOption");
    public static final String CLASS_INTERNAL_Unsafe = calc("jdk.internal.misc.Unsafe");
    public static final String CLASS_INTERNAL_Reflection = calc("jdk.internal.reflect.Reflection");
    public static final String CLASS_SUN_Reflection = calc("sun.reflect.Reflection");

    public static final String FIELD_MethodHandles_Lookup_IMPL_LOOKUP = calc("IMPL_LOOKUP");
    public static final String FIELD_URLClassLoader_ucp = calc("ucp");
    public static final String FIELD_Enum_$VALUES = calc("$VALUES");
    public static final String FIELD_Class_enumConstants = calc("enumConstants");
    public static final String FIELD_Class_enumConstantDirectory = calc("enumConstantDirectory");
    public static final String FIELD_Class_EnumVars = calc("enumVars");
    public static final String FIELD_Reflection_fieldFilterMap = calc("fieldFilterMap");
    public static final String FIELD_Reflection_methodFilterMap = calc("methodFilterMap");
    public static final String FIELD_Class_module = calc("module");
    public static final String FIELD_Module_EVERYONE_MODULE = calc("EVERYONE_MODULE");

    public static final String METHOD_Class_getDeclaredClasses0 = calc("getDeclaredClasses0", OPENJ9_RUNTIME, "getDeclaredClassesImpl");
    public static final String METHOD_Class_getDeclaredFields0 = calc("getDeclaredFields0", OPENJ9_RUNTIME, "getDeclaredFieldsImpl");
    public static final String METHOD_Class_getDeclaredConstructors0 = calc("getDeclaredConstructors0", OPENJ9_RUNTIME, "getDeclaredConstructorsImpl");
    public static final String METHOD_Class_getDeclaredMethods0 = calc("getDeclaredMethods0", OPENJ9_RUNTIME, "getDeclaredMethodsImpl");
    public static final String METHOD_InstrumentationImpl_loadAgent = calc("loadAgent");
    public static final String METHOD_Unsafe_defineAnonymousClass = calc("defineAnonymousClass");
    public static final String METHOD_MethodHandles_Lookup_defineHiddenClass = calc("defineHiddenClass");
    public static final String METHOD_URLClassPath_addURL = calc("addURL");
    public static final String METHOD_URLClassPath_getURLs = calc("getURLs");
    public static final String METHOD_ClassLoader_defineClass = calc("defineClass");
    public static final String METHOD_Module_implAddExportsOrOpens = calc("implAddExportsOrOpens");
    public static final String METHOD_Module_getPackages = calc("getPackages");
    public static final String METHOD_InternalUnsafe_staticFieldOffset = calc("staticFieldOffset");
    public static final String METHOD_InternalUnsafe_objectFieldOffset = calc("objectFieldOffset");


    /**
     * Prevent the java compiler from inlining static final strings.<br>
     * Also allows to use a different string depending on the JVM runtime.
     *
     * @param s    The string to prevent from inlining
     * @param args The arguments to check
     * @return The same string
     */
    private static String calc(final String s, final Object... args) {
        if (args.length % 2 != 0) throw new IllegalArgumentException("Arguments must be in pairs");
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof Boolean)) throw new IllegalArgumentException("Argument " + i + " must be a boolean");
            if (Boolean.TRUE.equals(args[i])) return args[i + 1].toString();
        }
        return s;
    }

}
