package net.lenni0451.reflect.exceptions;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class ConstructorInvocationException extends RuntimeException {

    public ConstructorInvocationException(final Constructor<?> constructor) {
        this(constructor.getDeclaringClass().getName(), Arrays.stream(constructor.getParameterTypes()).map(Class::getName).toArray(String[]::new));
    }

    public ConstructorInvocationException(final String owner, final String... args) {
        super("Could not invoke constructor '" + String.join(", ", args) + "' in class '" + owner + "'");
    }

    public ConstructorInvocationException cause(final Throwable cause) {
        this.initCause(cause);
        return this;
    }

}
