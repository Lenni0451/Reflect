package net.lenni0451.reflect.exceptions;

public class MethodNotFoundException extends RuntimeException {

    public MethodNotFoundException(final String owner, final String name) {
        super("Could not find method '" + name + "' in class '" + owner + "'");
    }

    public MethodNotFoundException(final String owner, final String name, final String... args) {
        super("Could not find method '" + name + "(" + String.join(", ", args) + ")' in class '" + owner + "'");
    }

}
