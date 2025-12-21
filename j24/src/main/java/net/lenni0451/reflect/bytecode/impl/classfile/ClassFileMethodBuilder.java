package net.lenni0451.reflect.bytecode.impl.classfile;

import net.lenni0451.reflect.bytecode.builder.MethodBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

public class ClassFileMethodBuilder implements MethodBuilder {

    private final CodeBuilder codeBuilder;

    public ClassFileMethodBuilder(final CodeBuilder codeBuilder) {
        this.codeBuilder = codeBuilder;
    }

    @Override
    public MethodBuilder return_() {
        this.codeBuilder.return_();
        return this;
    }

    @Override
    public MethodBuilder ireturn() {
        this.codeBuilder.ireturn();
        return this;
    }

    @Override
    public MethodBuilder lreturn() {
        this.codeBuilder.lreturn();
        return this;
    }

    @Override
    public MethodBuilder freturn() {
        this.codeBuilder.freturn();
        return this;
    }

    @Override
    public MethodBuilder dreturn() {
        this.codeBuilder.dreturn();
        return this;
    }

    @Override
    public MethodBuilder areturn() {
        this.codeBuilder.areturn();
        return this;
    }

    @Override
    public MethodBuilder dup() {
        this.codeBuilder.dup();
        return this;
    }

    @Override
    public MethodBuilder dupX1() {
        this.codeBuilder.dup_x1();
        return this;
    }

    @Override
    public MethodBuilder pop() {
        this.codeBuilder.pop();
        return this;
    }

    @Override
    public MethodBuilder aaload() {
        this.codeBuilder.aaload();
        return this;
    }

    @Override
    public MethodBuilder aastore() {
        this.codeBuilder.aastore();
        return this;
    }

    @Override
    public MethodBuilder aconstNull() {
        this.codeBuilder.aconst_null();
        return this;
    }

    @Override
    public MethodBuilder athrow() {
        this.codeBuilder.athrow();
        return this;
    }

    @Override
    public MethodBuilder i2l() {
        this.codeBuilder.i2l();
        return this;
    }

    @Override
    public MethodBuilder iconstM1() {
        this.codeBuilder.iconst_m1();
        return this;
    }

    @Override
    public MethodBuilder iconst0() {
        this.codeBuilder.iconst_0();
        return this;
    }

    @Override
    public MethodBuilder iconst1() {
        this.codeBuilder.iconst_1();
        return this;
    }

    @Override
    public MethodBuilder iconst2() {
        this.codeBuilder.iconst_2();
        return this;
    }

    @Override
    public MethodBuilder iconst3() {
        this.codeBuilder.iconst_3();
        return this;
    }

    @Override
    public MethodBuilder iconst4() {
        this.codeBuilder.iconst_4();
        return this;
    }

    @Override
    public MethodBuilder iconst5() {
        this.codeBuilder.iconst_5();
        return this;
    }

    @Override
    public MethodBuilder lconst0() {
        this.codeBuilder.lconst_0();
        return this;
    }

    @Override
    public MethodBuilder lconst1() {
        this.codeBuilder.lconst_1();
        return this;
    }

    @Override
    public MethodBuilder fconst0() {
        this.codeBuilder.fconst_0();
        return this;
    }

    @Override
    public MethodBuilder fconst1() {
        this.codeBuilder.fconst_1();
        return this;
    }

    @Override
    public MethodBuilder fconst2() {
        this.codeBuilder.fconst_2();
        return this;
    }

    @Override
    public MethodBuilder dconst0() {
        this.codeBuilder.dconst_0();
        return this;
    }

    @Override
    public MethodBuilder dconst1() {
        this.codeBuilder.dconst_1();
        return this;
    }

    @Override
    public MethodBuilder sipush(int value) {
        this.codeBuilder.sipush(value);
        return this;
    }

    @Override
    public MethodBuilder bipush(int value) {
        this.codeBuilder.bipush(value);
        return this;
    }

    @Override
    public MethodBuilder iload(int varIndex) {
        this.codeBuilder.iload(varIndex);
        return this;
    }

    @Override
    public MethodBuilder lload(int varIndex) {
        this.codeBuilder.lload(varIndex);
        return this;
    }

    @Override
    public MethodBuilder fload(int varIndex) {
        this.codeBuilder.fload(varIndex);
        return this;
    }

    @Override
    public MethodBuilder dload(int varIndex) {
        this.codeBuilder.dload(varIndex);
        return this;
    }

    @Override
    public MethodBuilder aload(int varIndex) {
        this.codeBuilder.aload(varIndex);
        return this;
    }

    @Override
    public MethodBuilder istore(int varIndex) {
        this.codeBuilder.istore(varIndex);
        return this;
    }

    @Override
    public MethodBuilder lstore(int varIndex) {
        this.codeBuilder.lstore(varIndex);
        return this;
    }

    @Override
    public MethodBuilder fstore(int varIndex) {
        this.codeBuilder.fstore(varIndex);
        return this;
    }

    @Override
    public MethodBuilder dstore(int varIndex) {
        this.codeBuilder.dstore(varIndex);
        return this;
    }

    @Override
    public MethodBuilder astore(int varIndex) {
        this.codeBuilder.astore(varIndex);
        return this;
    }

    @Override
    public MethodBuilder new_(String type) {
        this.codeBuilder.new_(ClassFileBuilder.getClassDesc(type));
        return this;
    }

    @Override
    public MethodBuilder checkcast(String type) {
        this.codeBuilder.checkcast(ClassFileBuilder.getClassDesc(type));
        return this;
    }

    @Override
    public MethodBuilder anewarray(String type) {
        this.codeBuilder.anewarray(ClassFileBuilder.getClassDesc(type));
        return this;
    }

    @Override
    public MethodBuilder putfield(String owner, String name, String descriptor) {
        this.codeBuilder.putfield(ClassFileBuilder.getClassDesc(owner), name, ClassFileBuilder.getClassDesc(descriptor));
        return this;
    }

    @Override
    public MethodBuilder putstatic(String owner, String name, String descriptor) {
        this.codeBuilder.putstatic(ClassFileBuilder.getClassDesc(owner), name, ClassFileBuilder.getClassDesc(descriptor));
        return this;
    }

    @Override
    public MethodBuilder getfield(String owner, String name, String descriptor) {
        this.codeBuilder.getfield(ClassFileBuilder.getClassDesc(owner), name, ClassFileBuilder.getClassDesc(descriptor));
        return this;
    }

    @Override
    public MethodBuilder getstatic(String owner, String name, String descriptor) {
        this.codeBuilder.getstatic(ClassFileBuilder.getClassDesc(owner), name, ClassFileBuilder.getClassDesc(descriptor));
        return this;
    }

    @Override
    public MethodBuilder invokespecial(String owner, String name, String descriptor, boolean isInterface) {
        this.codeBuilder.invokespecial(ClassFileBuilder.getClassDesc(owner), name, MethodTypeDesc.ofDescriptor(descriptor), isInterface);
        return this;
    }

    @Override
    public MethodBuilder invokeinterface(String owner, String name, String descriptor) {
        this.codeBuilder.invokeinterface(ClassFileBuilder.getClassDesc(owner), name, MethodTypeDesc.ofDescriptor(descriptor));
        return this;
    }

    @Override
    public MethodBuilder invokevirtual(String owner, String name, String descriptor) {
        this.codeBuilder.invokevirtual(ClassFileBuilder.getClassDesc(owner), name, MethodTypeDesc.ofDescriptor(descriptor));
        return this;
    }

    @Override
    public MethodBuilder invokestatic(String owner, String name, String descriptor, boolean isInterface) {
        this.codeBuilder.invokestatic(ClassFileBuilder.getClassDesc(owner), name, MethodTypeDesc.ofDescriptor(descriptor), isInterface);
        return this;
    }

    @Override
    public MethodBuilder ifne(BytecodeLabel label) {
        this.codeBuilder.ifne((Label) label.getHandle());
        return this;
    }

    @Override
    public MethodBuilder ifnonnull(BytecodeLabel label) {
        this.codeBuilder.ifnonnull((Label) label.getHandle());
        return this;
    }

    @Override
    public MethodBuilder goto_(BytecodeLabel label) {
        this.codeBuilder.goto_((Label) label.getHandle());
        return this;
    }

    @Override
    public BytecodeLabel newLabel() {
        return new BytecodeLabel(this.codeBuilder.newLabel());
    }

    @Override
    public MethodBuilder label(BytecodeLabel label) {
        this.codeBuilder.labelBinding((Label) label.getHandle());
        return this;
    }

    @Override
    public MethodBuilder ldc(Object value) {
        switch (value) {
            case ClassDesc classDesc -> this.codeBuilder.ldc(classDesc);
            case MethodHandleDesc methodHandleDesc -> this.codeBuilder.ldc(methodHandleDesc);
            case MethodTypeDesc methodTypeDesc -> this.codeBuilder.ldc(methodTypeDesc);
            case Double v -> this.codeBuilder.ldc(v);
            case DynamicConstantDesc<?> dynamicConstantDesc -> this.codeBuilder.ldc(dynamicConstantDesc);
            case Float v -> this.codeBuilder.ldc(v);
            case Integer i -> this.codeBuilder.ldc(i);
            case Long l -> this.codeBuilder.ldc(l);
            case String s -> this.codeBuilder.ldc(s);
            case BytecodeType type -> this.codeBuilder.ldc((ClassDesc) type.getHandle());
            case null -> throw new IllegalArgumentException("Cannot use null as value for ldc");
            default -> throw new IllegalArgumentException("Unsupported value type for ldc: " + value.getClass().getName());
        }
        return this;
    }

    @Override
    public MethodBuilder tryCatch(BytecodeLabel start, BytecodeLabel end, BytecodeLabel handler, String type) {
        this.codeBuilder.exceptionCatch((Label) start.getHandle(), (Label) end.getHandle(), (Label) handler.getHandle(), ClassFileBuilder.getClassDesc(type));
        return this;
    }

    @Override
    public MethodBuilder maxs(int maxStack, int maxLocals) {
        return this;
    }

}
