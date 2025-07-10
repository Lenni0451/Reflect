package net.lenni0451.reflect.bytecode;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BytecodeBuilderTest {

    @Test
    void test() {
        BytecodeBuilder builder = BytecodeBuilder.get();
        BuiltClass builtClass = builder.class_(builder.opcode("ACC_PUBLIC"), "net/lenni0451/reflect/bytecode/BytecodeBuilderTestSupplier", null, "java/lang/Object", new String[]{"java/util/function/Supplier"}, clazz -> {
            clazz.method(builder.opcode("ACC_PUBLIC"), "<init>", "()V", null, null, method -> method
                    .var(builder.opcode("ALOAD"), 0)
                    .method(builder.opcode("INVOKESPECIAL"), "java/lang/Object", "<init>", "()V", false)
                    .insn(builder.opcode("RETURN"))
                    .maxs(1, 1));
            clazz.method(builder.opcode("ACC_PUBLIC"), "get", "()Ljava/lang/Object;", null, null, method -> {
                BytecodeLabel start = builder.label();
                BytecodeLabel end = builder.label();
                BytecodeLabel handler = builder.label();

                method
                        .jump(builder.opcode("GOTO"), start)
                        .label(start)
                        .ldc("Hello World")
                        .insn(builder.opcode("ARETURN"))
                        .label(end)
                        .label(handler)
                        .insn(builder.opcode("ACONST_NULL"))
                        .insn(builder.opcode("ARETURN"))
                        .tryCatch(start, end, handler, "java/lang/Exception")
                        .maxs(1, 1);
            });
        });

        Class<?> clazz = builtClass.defineAnonymous(BytecodeBuilderTest.class);
        Supplier<String> supplier = (Supplier<String>) assertDoesNotThrow(() -> clazz.getDeclaredConstructor().newInstance());
        assertEquals("Hello World", supplier.get());
    }

}
