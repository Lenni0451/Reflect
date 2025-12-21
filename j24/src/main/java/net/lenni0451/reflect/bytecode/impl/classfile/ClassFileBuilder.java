package net.lenni0451.reflect.bytecode.impl.classfile;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassSignature;
import java.lang.classfile.Interfaces;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.function.Consumer;

public class ClassFileBuilder implements BytecodeBuilder {

    public static ClassDesc getClassDesc(final String name) {
        if (name.startsWith("[") || name.length() == 1) {
            return ClassDesc.ofDescriptor(name);
        } else if (name.startsWith("L") && name.endsWith(";")) {
            return ClassDesc.ofDescriptor(name);
        } else {
            return ClassDesc.ofInternalName(name);
        }
    }


    @Override
    public BuiltClass class_(int access, String name, String signature, String superName, String[] interfaces, Consumer<ClassBuilder> consumer) {
        ClassDesc desc = ClassFileBuilder.getClassDesc(name);
        return new ClassFileBuiltClass(name, ClassFile.of().build(desc, classBuilder -> {
            classBuilder
                    .withFlags(access)
                    .withSuperclass(ClassFileBuilder.getClassDesc(superName));
            if (signature != null) {
                classBuilder.with(SignatureAttribute.of(ClassSignature.parseFrom(signature)));
            }
            if (interfaces != null && interfaces.length > 0) {
                classBuilder.with(Interfaces.ofSymbols(Arrays.stream(interfaces).map(ClassFileBuilder::getClassDesc).toList()));
            }
            consumer.accept(new ClassFileClassBuilder(name, classBuilder));
        }));
    }

    @Override
    public BytecodeType type(String descriptor) {
        return new BytecodeType(ClassFileBuilder.getClassDesc(descriptor));
    }

    @Override
    public int opcode(String name) {
        if (name.startsWith("ACC_")) {
            String accessName = name.substring(4);
            return AccessFlag.valueOf(accessName).mask();
        } else {
            return Opcode.valueOf(name).bytecode();
        }
    }

}
