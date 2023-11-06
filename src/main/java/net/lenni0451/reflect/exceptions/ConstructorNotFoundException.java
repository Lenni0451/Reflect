package net.lenni0451.reflect.exceptions;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class ConstructorNotFoundException extends RuntimeException {

    public ConstructorNotFoundException(final String owner, @Nullable final Class<?>... args) {
        this(
                owner,
                Optional
                        .ofNullable(args)
                        .map(Arrays::stream)
                        .map(s -> s.map(Class::getSimpleName))
                        .map(s -> s.toArray(String[]::new))
                        .orElse(new String[0])
        );
    }

    public ConstructorNotFoundException(final String owner, final String... args) {
        super("Could not find constructor '(" + String.join(", ", args) + ")' in class '" + owner + "'");
    }

}
