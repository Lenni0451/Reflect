package net.lenni0451.reflect.accessor;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * Utils for creating accessors for fields and methods.
 */
@ApiStatus.Internal
class AccessorUtils {

    public static void addConstructor(final BytecodeBuilder builder, final ClassBuilder cb, @Nullable final Supplier<Class<?>> instanceType, final boolean isStatic) {
        if (isStatic || instanceType == null) {
            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class), null, null, mb -> mb
                    .var(builder.opcode("ALOAD"), 0)
                    .method(builder.opcode("INVOKESPECIAL"), slash(Object.class), "<init>", mdesc(void.class), false)
                    .insn(builder.opcode("RETURN"))
                    .maxs(1, 1)
            );
        } else {
            cb.field(builder.opcode("ACC_PRIVATE", "ACC_FINAL"), "instance", desc(instanceType.get()), null, null, fb -> {});

            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, instanceType.get()), null, null, mb -> mb
                    .var(builder.opcode("ALOAD"), 0)
                    .method(builder.opcode("INVOKESPECIAL"), slash(Object.class), "<init>", mdesc(void.class), false)
                    .var(builder.opcode("ALOAD"), 0)
                    .var(builder.opcode("ALOAD"), 1)
                    .field(builder.opcode("PUTFIELD"), cb.getName(), "instance", desc(instanceType.get()))
                    .insn(builder.opcode("RETURN"))
                    .maxs(2, 2)
            );
        }
    }

}
