package net.lenni0451.reflect.exceptions;

public class FieldNotFoundException extends RuntimeException {

    public FieldNotFoundException(final String owner, final String... args) {
        super("Could not find field '" + String.join(", ", args) + "' in class '" + owner + "'");
    }

}
