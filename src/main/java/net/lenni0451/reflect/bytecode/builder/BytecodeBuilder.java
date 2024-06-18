package net.lenni0451.reflect.bytecode.builder;

import lombok.SneakyThrows;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

@ApiStatus.Experimental
public interface BytecodeBuilder {

    @SneakyThrows
    static BytecodeBuilder get() {
        if (Classes.byName("org.objectweb.asm.Opcodes") != null || Classes.byName("jdk.internal.org.objectweb.asm.Opcodes") != null) {
            Class<?> impl = Classes.forName("net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder");
            return (BytecodeBuilder) TRUSTED_LOOKUP.findConstructor(impl, MethodType.methodType(void.class)).invoke();
        }
        throw new UnsupportedOperationException("No supported bytecode library found");
    }


    BuiltClass class_(final int access, final String name, final String signature, final String superName, final String[] interfaces, final Consumer<ClassBuilder> consumer);

    BytecodeLabel label();

    int opcode(final String name);

}
