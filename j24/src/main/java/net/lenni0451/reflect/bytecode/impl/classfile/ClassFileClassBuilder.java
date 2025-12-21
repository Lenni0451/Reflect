package net.lenni0451.reflect.bytecode.impl.classfile;

import lombok.Getter;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.builder.FieldBuilder;
import net.lenni0451.reflect.bytecode.builder.MethodBuilder;

import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public class ClassFileClassBuilder implements ClassBuilder {

    private final String name;
    @Getter
    private final java.lang.classfile.ClassBuilder classBuilder;

    public ClassFileClassBuilder(final String name, final java.lang.classfile.ClassBuilder classBuilder) {
        this.name = name;
        this.classBuilder = classBuilder;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void field(int access, String name, String descriptor, String signature, Object defaultValue, Consumer<FieldBuilder> consumer) {
        this.classBuilder.withField(name, ClassFileBuilder.getClassDesc(descriptor), fieldBuilder -> {
            fieldBuilder.withFlags(access);
            if (signature != null) {
                fieldBuilder.with(SignatureAttribute.of(Signature.parseFrom(signature)));
            }
            if (defaultValue != null) {
                switch (defaultValue) {
                    case Boolean b -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().intEntry(b ? 1 : 0)));
                    case Byte b -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().intEntry(b)));
                    case Short s -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().intEntry(s)));
                    case Character c -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().intEntry(c)));
                    case Integer i -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().intEntry(i)));
                    case Long l -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().longEntry(l)));
                    case Float f -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().floatEntry(f)));
                    case Double d -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().doubleEntry(d)));
                    case String s -> fieldBuilder.with(ConstantValueAttribute.of(this.classBuilder.constantPool().stringEntry(s)));
                    default -> throw new IllegalArgumentException("Unsupported constant value type: " + defaultValue.getClass().getName());
                }
            }
            consumer.accept(new ClassFileFieldBuilder(fieldBuilder));
        });
    }

    @Override
    public void method(int access, String name, String descriptor, String signature, String[] exceptions, Consumer<MethodBuilder> consumer) {
        this.classBuilder.withMethod(name, MethodTypeDesc.ofDescriptor(descriptor), access, methodBuilder -> {
            if (signature != null) {
                methodBuilder.with(SignatureAttribute.of(MethodSignature.parseFrom(signature)));
            }
            if (exceptions != null && exceptions.length > 0) {
                ClassDesc[] exceptionDescs = new ClassDesc[exceptions.length];
                for (int i = 0; i < exceptions.length; i++) exceptionDescs[i] = ClassFileBuilder.getClassDesc(exceptions[i]);
                methodBuilder.with(ExceptionsAttribute.ofSymbols(exceptionDescs));
            }
            methodBuilder.withCode(codeBuilder -> {
                consumer.accept(new ClassFileMethodBuilder(codeBuilder));
            });
        });
    }

}
