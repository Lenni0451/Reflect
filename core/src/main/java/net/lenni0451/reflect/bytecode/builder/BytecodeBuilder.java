package net.lenni0451.reflect.bytecode.builder;

import lombok.SneakyThrows;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

@ApiStatus.Experimental
public interface BytecodeBuilder {

    @SneakyThrows
    static BytecodeBuilder get() {
        if (Classes.byName("org.objectweb.asm.Opcodes", BytecodeBuilder.class.getClassLoader()) != null
                || Classes.byName("jdk.internal.org.objectweb.asm.Opcodes", BytecodeBuilder.class.getClassLoader()) != null) {
            Class<?> impl = Classes.forName("net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder", BytecodeBuilder.class.getClassLoader());
            return (BytecodeBuilder) TRUSTED_LOOKUP.findConstructor(impl, MethodType.methodType(void.class)).invoke();
        }
        throw new UnsupportedOperationException("No supported bytecode library found");
    }


    BuiltClass class_(final int access, final String name, final String signature, final String superName, final String[] interfaces, final Consumer<ClassBuilder> consumer);

    BytecodeLabel label();

    BytecodeType type(final String descriptor);

    int opcode(final String name);

    default int opcode(final String name, final String... or) {
        int opcode = this.opcode(name);
        for (String s : or) opcode |= this.opcode(s);
        return opcode;
    }

}
