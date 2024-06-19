package net.lenni0451.reflect.bytecode.builder;

import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.jetbrains.annotations.ApiStatus;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

@ApiStatus.Experimental
public interface MethodBuilder {

    MethodBuilder insn(final int opcode);

    MethodBuilder int_(final int opcode, final int value);

    default MethodBuilder intPush(final BytecodeBuilder builder, final int i) {
        if (i >= -1 && i <= 5) return this.insn(builder.opcode("ICONST_" + i));
        if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) return this.int_(builder.opcode("BIPUSH"), i);
        if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) return this.int_(builder.opcode("SIPUSH"), i);
        return this.ldc(i);
    }

    MethodBuilder var(final int opcode, final int varIndex);

    MethodBuilder type(final int opcode, final String type);

    default MethodBuilder box(final BytecodeBuilder builder, final Class<?> primitive) {
        Class<?> boxed = boxed(primitive);
        if (boxed != primitive) {
            this.method(builder.opcode("INVOKESTATIC"), slash(boxed), "valueOf", mdesc(boxed, primitive), false);
        }
        return this;
    }

    default MethodBuilder unbox(final BytecodeBuilder builder, final Class<?> primitive) {
        Class<?> boxed = boxed(primitive);
        if (boxed != primitive) {
            this.method(builder.opcode("INVOKEVIRTUAL"), slash(boxed), primitive.getSimpleName() + "Value", mdesc(primitive), false);
        }
        return this;
    }

    MethodBuilder field(final int opcode, final String owner, final String name, final String descriptor);

    MethodBuilder method(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);

    MethodBuilder jump(final int opcode, final BytecodeLabel label);

    MethodBuilder label(final BytecodeLabel label);

    MethodBuilder ldc(final Object value);

    default MethodBuilder typeLdc(final BytecodeBuilder builder, final Class<?> clazz) {
        Class<?> boxed = boxed(clazz);
        if (boxed == clazz) {
            this.ldc(builder.type(desc(clazz)));
        } else {
            this.field(builder.opcode("GETSTATIC"), slash(boxed), "TYPE", desc(Class.class));
        }
        return this;
    }

    MethodBuilder iinc(final int varIndex, final int increment);

    MethodBuilder multiANewArray(final String descriptor, final int dimensions);

    MethodBuilder tryCatch(final BytecodeLabel start, final BytecodeLabel end, final BytecodeLabel handler, final String type);

    MethodBuilder maxs(final int maxStack, final int maxLocals);

}
