package net.lenni0451.reflect.bytecode;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicTest {

    @Test
    void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BytecodeBuilder builder = BytecodeBuilder.get();
        Class<?> clazz = builder.class_(builder.opcode("ACC_PUBLIC"), "net/lenni0451/reflect/bytecode/TestClass", null, "java/lang/Object", new String[0], cb -> {
            cb.method(builder.opcode("ACC_PUBLIC") | builder.opcode("ACC_STATIC"), "testMethod", "(Z)Ljava/lang/String;", null, null, mb -> {
                BytecodeLabel elseLabel = mb.newLabel();
                BytecodeLabel endLabel = mb.newLabel();
                mb.iload(0);
                mb.ifne(elseLabel);
                mb.ldc("Arg is false");
                mb.goto_(endLabel);
                mb.label(elseLabel);
                mb.ldc("Arg is true");
                mb.label(endLabel);
                mb.invokestatic("java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                mb.invokevirtual("java/lang/String", "toLowerCase", "()Ljava/lang/String;");
                mb.areturn();
                mb.maxs(1, 1);
            });
        }).defineMetafactory(BasicTest.class);
        Method testMethod = clazz.getDeclaredMethod("testMethod", boolean.class);
        assertEquals("arg is false", testMethod.invoke(null, false));
        assertEquals("arg is true", testMethod.invoke(null, true));
    }

}
