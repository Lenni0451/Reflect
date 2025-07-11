package net.lenni0451.reflect.bytecode.impl.classfile;

import lombok.Getter;
import net.lenni0451.reflect.bytecode.builder.FieldBuilder;

public class ClassFileFieldBuilder implements FieldBuilder {

    @Getter
    private final java.lang.classfile.FieldBuilder fieldBuilder;

    public ClassFileFieldBuilder(final java.lang.classfile.FieldBuilder fieldBuilder) {
        this.fieldBuilder = fieldBuilder;
    }

}
