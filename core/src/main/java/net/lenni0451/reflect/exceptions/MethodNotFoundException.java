package net.lenni0451.reflect.exceptions;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class MethodNotFoundException extends RuntimeException {

    public MethodNotFoundException(final String owner, final String name) {
        super("Could not find method '" + name + "' in class '" + owner + "'");
    }

    public MethodNotFoundException(final String owner, @Nullable final String name, @Nullable final Class<?>... args) {
        this(
                owner,
                name,
                Optional
                        .ofNullable(args)
                        .map(Arrays::stream)
                        .map(s -> s.map(Class::getSimpleName))
                        .map(s -> s.toArray(String[]::new))
                        .orElse(new String[0])
        );
    }

    public MethodNotFoundException(final String owner, @Nullable final String name, final String... args) {
        super("Could not find method '" + (name == null ? "" : name) + "(" + String.join(", ", args) + ")' in class '" + owner + "'");
    }

}
