package net.lenni0451.reflect.bytecode.builder;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Experimental
public interface ClassBuilder {

    void field(final int access, final String name, final String descriptor, final String signature, final Object defaultValue, final Consumer<FieldBuilder> consumer);

    void method(final int access, final String name, final String descriptor, final String signature, final String[] exceptions, final Consumer<MethodBuilder> consumer);

}
