package net.lenni0451.reflect.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * Utility class for invoking MethodHandles.
 */
public class Invoker {

    /**
     * Invoke a method handle with the given arguments. This method will handle varargs if the method handle is a varargs collector.
     *
     * @param handle The method handle to invoke
     * @param args   The arguments to pass to the method handle
     * @param <T>    The expected return type of the method handle
     * @return The result of the method handle invocation
     * @throws Throwable If the method handle invocation throws an exception
     */
    public static <T> T dynamicInvoke(final MethodHandle handle, final Object[] args) throws Throwable {
        if (handle.isVarargsCollector() && args != null) {
            MethodType type = handle.type();
            int parameterCount = type.parameterCount();
            if (args.length == parameterCount) {
                Object lastArg = args[args.length - 1];
                Class<?> varargType = type.parameterType(parameterCount - 1);
                if (lastArg == null || varargType.isInstance(lastArg)) {
                    return (T) handle.asFixedArity().invokeWithArguments(args);
                }
            }
        }
        return (T) handle.invokeWithArguments(args);
    }

}
