package net.lenni0451.reflect.exceptions;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocationException extends RuntimeException {

    public MethodInvocationException(final Method method) {
        this(method.getDeclaringClass().getName(), method.getName(), Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new));
    }

    public MethodInvocationException(final String owner, final String name) {
        super("Could not invoke method '" + name + "' in class '" + owner + "'");
    }

    public MethodInvocationException(final String owner, final String name, final String... args) {
        super("Could not invoke method '" + name + "(" + String.join(", ", args) + ")' in class '" + owner + "'");
    }

    public MethodInvocationException cause(final Throwable cause) {
        this.initCause(cause);
        return this;
    }

}
