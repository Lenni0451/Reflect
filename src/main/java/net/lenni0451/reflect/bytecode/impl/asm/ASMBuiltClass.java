package net.lenni0451.reflect.bytecode.impl.asm;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.CLASS_ClassWriter;

class ASMBuiltClass implements BuiltClass {

    private final Object classWriter;

    public ASMBuiltClass(final Object classWriter) {
        this.classWriter = classWriter;
    }

    @Override
    @SneakyThrows
    public byte[] toBytes() {
        MethodHandle toByteArray = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "toByteArray", MethodType.methodType(byte[].class));
        return (byte[]) toByteArray.invoke(this.classWriter);
    }

}
