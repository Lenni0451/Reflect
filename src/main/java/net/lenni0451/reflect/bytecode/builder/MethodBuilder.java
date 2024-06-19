package net.lenni0451.reflect.bytecode.builder;

import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.jetbrains.annotations.ApiStatus;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.desc;

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

    MethodBuilder field(final int opcode, final String owner, final String name, final String descriptor);

    MethodBuilder method(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);

    MethodBuilder jump(final int opcode, final BytecodeLabel label);

    MethodBuilder label(final BytecodeLabel label);

    MethodBuilder ldc(final Object value);

    default MethodBuilder typeLdc(final BytecodeBuilder builder, final Class<?> clazz) {
        if (clazz == void.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Void", "TYPE", "Ljava/lang/Class;");
        else if (clazz == boolean.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
        else if (clazz == byte.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
        else if (clazz == short.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Short", "TYPE", "Ljava/lang/Class;");
        else if (clazz == char.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Character", "TYPE", "Ljava/lang/Class;");
        else if (clazz == int.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        else if (clazz == long.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Long", "TYPE", "Ljava/lang/Class;");
        else if (clazz == float.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Float", "TYPE", "Ljava/lang/Class;");
        else if (clazz == double.class) return this.field(builder.opcode("GETSTATIC"), "java/lang/Double", "TYPE", "Ljava/lang/Class;");
        else return this.ldc(builder.type(desc(clazz)));
    }

    MethodBuilder iinc(final int varIndex, final int increment);

    MethodBuilder multiANewArray(final String descriptor, final int dimensions);

    MethodBuilder tryCatch(final BytecodeLabel start, final BytecodeLabel end, final BytecodeLabel handler, final String type);

    MethodBuilder maxs(final int maxStack, final int maxLocals);

}
