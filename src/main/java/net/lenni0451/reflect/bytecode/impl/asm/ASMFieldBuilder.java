package net.lenni0451.reflect.bytecode.impl.asm;

import net.lenni0451.reflect.bytecode.builder.FieldBuilder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class ASMFieldBuilder implements FieldBuilder {

    private final Object fieldVisitor;

    public ASMFieldBuilder(final Object fieldVisitor) {
        this.fieldVisitor = fieldVisitor;
    }

    public Object getFieldVisitor() {
        return this.fieldVisitor;
    }

}
