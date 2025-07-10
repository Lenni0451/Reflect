package net.lenni0451.reflect.bytecode.impl.asm;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.builder.FieldBuilder;
import net.lenni0451.reflect.bytecode.builder.MethodBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.*;

@ApiStatus.Internal
class ASMClassBuilder implements ClassBuilder {

    private final ToIntFunction<String> opcodeResolver;
    private final Object classWriter;
    private final String name;

    public ASMClassBuilder(final ToIntFunction<String> opcodeResolver, final Object classWriter, final String name) {
        this.opcodeResolver = opcodeResolver;
        this.classWriter = classWriter;
        this.name = name;
    }

    public Object getClassWriter() {
        return this.classWriter;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    @SneakyThrows
    public void field(int access, String name, String descriptor, String signature, Object defaultValue, Consumer<FieldBuilder> consumer) {
        MethodHandle visitField = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visitField", MethodType.methodType(CLASS_FieldVisitor, int.class, String.class, String.class, String.class, Object.class));
        MethodHandle visitEnd = TRUSTED_LOOKUP.findVirtual(CLASS_FieldVisitor, "visitEnd", MethodType.methodType(void.class));

        ASMFieldBuilder builder = new ASMFieldBuilder(visitField.invoke(this.classWriter, access, name, descriptor, signature, defaultValue));
        consumer.accept(builder);
        visitEnd.invoke(builder.getFieldVisitor());
    }

    @Override
    @SneakyThrows
    public void method(int access, String name, String descriptor, String signature, String[] exceptions, Consumer<MethodBuilder> consumer) {
        MethodHandle visitMethod = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visitMethod", MethodType.methodType(CLASS_MethodVisitor, int.class, String.class, String.class, String.class, String[].class));
        MethodHandle visitCode = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitCode", MethodType.methodType(void.class));
        MethodHandle visitEnd = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitEnd", MethodType.methodType(void.class));

        ASMMethodBuilder builder = new ASMMethodBuilder(this.opcodeResolver, visitMethod.invoke(this.classWriter, access, name, descriptor, signature, exceptions));
        visitCode.invoke(builder.getMethodVisitor());
        consumer.accept(builder);
        visitEnd.invoke(builder.getMethodVisitor());
    }

}
