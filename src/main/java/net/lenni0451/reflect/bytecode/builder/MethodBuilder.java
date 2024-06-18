package net.lenni0451.reflect.bytecode.builder;

import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MethodBuilder {

    MethodBuilder insn(final int opcode);

    MethodBuilder int_(final int opcode, final int value);

    MethodBuilder var(final int opcode, final int varIndex);

    MethodBuilder type(final int opcode, final String type);

    MethodBuilder field(final int opcode, final String owner, final String name, final String descriptor);

    MethodBuilder method(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);

    MethodBuilder jump(final int opcode, final BytecodeLabel label);

    MethodBuilder label(final BytecodeLabel label);

    MethodBuilder ldc(final Object value);

    MethodBuilder iinc(final int varIndex, final int increment);

    MethodBuilder multiANewArray(final String descriptor, final int dimensions);

    MethodBuilder tryCatch(final BytecodeLabel start, final BytecodeLabel end, final BytecodeLabel handler, final String type);

    MethodBuilder maxs(final int maxStack, final int maxLocals);

}
