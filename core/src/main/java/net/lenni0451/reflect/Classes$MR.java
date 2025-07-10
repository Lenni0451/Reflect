package net.lenni0451.reflect;

import net.lenni0451.reflect.exceptions.MethodNotFoundException;
import net.lenni0451.reflect.utils.FieldInitializer;
import sun.reflect.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * This class contains methods which need to be replaced by other implementations for newer JDKs.
 */
class Classes$MR {

    private static final SecurityManager SECURITY_MANAGER = new SecurityManager();
    private static final MethodHandle GET_CLASS_CONTEXT = FieldInitializer.reqInit(
            () -> JavaBypass.TRUSTED_LOOKUP.findVirtual(SecurityManager.class, "getClassContext", MethodType.methodType(Class[].class)),
            () -> new MethodNotFoundException(SecurityManager.class.getName(), "getClassContext")
    );

    public static Class<?> getCallerClass(final int depth) throws Throwable {
        try {
            return Reflection.getCallerClass(depth + 3);
        } catch (Throwable ignored) {
        }
        try {
            Class<?>[] classes = (Class<?>[]) GET_CLASS_CONTEXT.invokeExact(SECURITY_MANAGER);
            return classes[depth + 2];
        } catch (ArrayIndexOutOfBoundsException e) {
            //Don't crash if the depth is too high
            return null;
        }
    }

}
