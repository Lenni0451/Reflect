package net.lenni0451.reflect.bytecode.impl.asm;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.MethodBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.ToIntFunction;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.CLASS_Label;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.CLASS_MethodVisitor;

@ApiStatus.Internal
class ASMMethodBuilder implements MethodBuilder {

    private final ToIntFunction<String> opcodeResolver;
    private final Object methodVisitor;

    public ASMMethodBuilder(final ToIntFunction<String> opcodeResolver, final Object methodVisitor) {
        this.opcodeResolver = opcodeResolver;
        this.methodVisitor = methodVisitor;
    }

    public Object getMethodVisitor() {
        return this.methodVisitor;
    }

    @Override
    public MethodBuilder return_() {
        return this.insn(this.opcodeResolver.applyAsInt("RETURN"));
    }

    @Override
    public MethodBuilder ireturn() {
        return this.insn(this.opcodeResolver.applyAsInt("IRETURN"));
    }

    @Override
    public MethodBuilder lreturn() {
        return this.insn(this.opcodeResolver.applyAsInt("LRETURN"));
    }

    @Override
    public MethodBuilder freturn() {
        return this.insn(this.opcodeResolver.applyAsInt("FRETURN"));
    }

    @Override
    public MethodBuilder dreturn() {
        return this.insn(this.opcodeResolver.applyAsInt("DRETURN"));
    }

    @Override
    public MethodBuilder areturn() {
        return this.insn(this.opcodeResolver.applyAsInt("ARETURN"));
    }

    @Override
    public MethodBuilder dup() {
        return this.insn(this.opcodeResolver.applyAsInt("DUP"));
    }

    @Override
    public MethodBuilder dupX1() {
        return this.insn(this.opcodeResolver.applyAsInt("DUP_X1"));
    }

    @Override
    public MethodBuilder pop() {
        return this.insn(this.opcodeResolver.applyAsInt("POP"));
    }

    @Override
    public MethodBuilder aaload() {
        return this.insn(this.opcodeResolver.applyAsInt("AALOAD"));
    }

    @Override
    public MethodBuilder aastore() {
        return this.insn(this.opcodeResolver.applyAsInt("AASTORE"));
    }

    @Override
    public MethodBuilder aconstNull() {
        return this.insn(this.opcodeResolver.applyAsInt("ACONST_NULL"));
    }

    @Override
    public MethodBuilder athrow() {
        return this.insn(this.opcodeResolver.applyAsInt("ATHROW"));
    }

    @Override
    public MethodBuilder i2l() {
        return this.insn(this.opcodeResolver.applyAsInt("I2L"));
    }

    @Override
    public MethodBuilder iconstM1() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_M1"));
    }

    @Override
    public MethodBuilder iconst0() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_0"));
    }

    @Override
    public MethodBuilder iconst1() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_1"));
    }

    @Override
    public MethodBuilder iconst2() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_2"));
    }

    @Override
    public MethodBuilder iconst3() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_3"));
    }

    @Override
    public MethodBuilder iconst4() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_4"));
    }

    @Override
    public MethodBuilder iconst5() {
        return this.insn(this.opcodeResolver.applyAsInt("ICONST_5"));
    }

    @Override
    public MethodBuilder lconst0() {
        return this.insn(this.opcodeResolver.applyAsInt("LCONST_0"));
    }

    @Override
    public MethodBuilder lconst1() {
        return this.insn(this.opcodeResolver.applyAsInt("LCONST_1"));
    }

    @Override
    public MethodBuilder fconst0() {
        return this.insn(this.opcodeResolver.applyAsInt("FCONST_0"));
    }

    @Override
    public MethodBuilder fconst1() {
        return this.insn(this.opcodeResolver.applyAsInt("FCONST_1"));
    }

    @Override
    public MethodBuilder fconst2() {
        return this.insn(this.opcodeResolver.applyAsInt("FCONST_2"));
    }

    @Override
    public MethodBuilder dconst0() {
        return this.insn(this.opcodeResolver.applyAsInt("DCONST_0"));
    }

    @Override
    public MethodBuilder dconst1() {
        return this.insn(this.opcodeResolver.applyAsInt("DCONST_1"));
    }

    @Override
    public MethodBuilder dconst2() {
        return this.insn(this.opcodeResolver.applyAsInt("DCONST_2"));
    }

    @Override
    public MethodBuilder sipush(int value) {
        return this.int_(this.opcodeResolver.applyAsInt("SIPUSH"), value);
    }

    @Override
    public MethodBuilder bipush(int value) {
        return this.int_(this.opcodeResolver.applyAsInt("BIPUSH"), value);
    }

    @Override
    public MethodBuilder iload(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("ILOAD"), varIndex);
    }

    @Override
    public MethodBuilder lload(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("LLOAD"), varIndex);
    }

    @Override
    public MethodBuilder fload(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("FLOAD"), varIndex);
    }

    @Override
    public MethodBuilder dload(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("DLOAD"), varIndex);
    }

    @Override
    public MethodBuilder aload(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("ALOAD"), varIndex);
    }

    @Override
    public MethodBuilder istore(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("ISTORE"), varIndex);
    }

    @Override
    public MethodBuilder lstore(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("LSTORE"), varIndex);
    }

    @Override
    public MethodBuilder fstore(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("FSTORE"), varIndex);
    }

    @Override
    public MethodBuilder dstore(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("DSTORE"), varIndex);
    }

    @Override
    public MethodBuilder astore(int varIndex) {
        return this.var(this.opcodeResolver.applyAsInt("ASTORE"), varIndex);
    }

    @Override
    public MethodBuilder new_(String type) {
        return this.type(this.opcodeResolver.applyAsInt("NEW"), type);
    }

    @Override
    public MethodBuilder checkcast(String type) {
        return this.type(this.opcodeResolver.applyAsInt("CHECKCAST"), type);
    }

    @Override
    public MethodBuilder anewarray(String type) {
        return this.type(this.opcodeResolver.applyAsInt("ANEWARRAY"), type);
    }

    @Override
    public MethodBuilder putfield(String owner, String name, String descriptor) {
        return this.field(this.opcodeResolver.applyAsInt("PUTFIELD"), owner, name, descriptor);
    }

    @Override
    public MethodBuilder putstatic(String owner, String name, String descriptor) {
        return this.field(this.opcodeResolver.applyAsInt("PUTSTATIC"), owner, name, descriptor);
    }

    @Override
    public MethodBuilder getfield(String owner, String name, String descriptor) {
        return this.field(this.opcodeResolver.applyAsInt("GETFIELD"), owner, name, descriptor);
    }

    @Override
    public MethodBuilder getstatic(String owner, String name, String descriptor) {
        return this.field(this.opcodeResolver.applyAsInt("GETSTATIC"), owner, name, descriptor);
    }

    @Override
    public MethodBuilder invokespecial(String owner, String name, String descriptor, boolean isInterface) {
        return this.method(this.opcodeResolver.applyAsInt("INVOKESPECIAL"), owner, name, descriptor, isInterface);
    }

    @Override
    public MethodBuilder invokeinterface(String owner, String name, String descriptor, boolean isInterface) {
        return this.method(this.opcodeResolver.applyAsInt("INVOKEINTERFACE"), owner, name, descriptor, isInterface);
    }

    @Override
    public MethodBuilder invokevirtual(String owner, String name, String descriptor, boolean isInterface) {
        return this.method(this.opcodeResolver.applyAsInt("INVOKEVIRTUAL"), owner, name, descriptor, isInterface);
    }

    @Override
    public MethodBuilder invokestatic(String owner, String name, String descriptor, boolean isInterface) {
        return this.method(this.opcodeResolver.applyAsInt("INVOKESTATIC"), owner, name, descriptor, isInterface);
    }

    @Override
    public MethodBuilder ifnonnull(BytecodeLabel label) {
        return this.jump(this.opcodeResolver.applyAsInt("IFNONNULL"), label);
    }

    @Override
    public MethodBuilder goto_(BytecodeLabel label) {
        return this.jump(this.opcodeResolver.applyAsInt("GOTO"), label);
    }

    @Override
    @SneakyThrows
    public MethodBuilder label(BytecodeLabel label) {
        MethodHandle visitLabel = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitLabel", MethodType.methodType(void.class, CLASS_Label));
        visitLabel.invoke(this.methodVisitor, label.getHandle());
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder ldc(Object value) {
        if (value instanceof BytecodeType) value = ((BytecodeType) value).getHandle();
        MethodHandle visitLdcInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitLdcInsn", MethodType.methodType(void.class, Object.class));
        visitLdcInsn.invoke(this.methodVisitor, value);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder tryCatch(BytecodeLabel start, BytecodeLabel end, BytecodeLabel handler, String type) {
        MethodHandle visitTryCatchBlock = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTryCatchBlock", MethodType.methodType(void.class, CLASS_Label, CLASS_Label, CLASS_Label, String.class));
        visitTryCatchBlock.invoke(this.methodVisitor, start.getHandle(), end.getHandle(), handler.getHandle(), type);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder maxs(int maxStack, int maxLocals) {
        MethodHandle visitMaxs = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMaxs", MethodType.methodType(void.class, int.class, int.class));
        visitMaxs.invoke(this.methodVisitor, maxStack, maxLocals);
        return this;
    }

    //Private methods for different instruction types
    //They are invoked to make the code above cleaner and more readable

    @SneakyThrows
    private MethodBuilder insn(int opcode) {
        MethodHandle visitInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitInsn", MethodType.methodType(void.class, int.class));
        visitInsn.invoke(this.methodVisitor, opcode);
        return this;
    }

    @SneakyThrows
    private MethodBuilder int_(int opcode, int value) {
        MethodHandle visitIntInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIntInsn", MethodType.methodType(void.class, int.class, int.class));
        visitIntInsn.invoke(this.methodVisitor, opcode, value);
        return this;
    }

    @SneakyThrows
    private MethodBuilder var(int opcode, int varIndex) {
        MethodHandle visitVarInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitVarInsn", MethodType.methodType(void.class, int.class, int.class));
        visitVarInsn.invoke(this.methodVisitor, opcode, varIndex);
        return this;
    }

    @SneakyThrows
    private MethodBuilder type(int opcode, String type) {
        MethodHandle visitTypeInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTypeInsn", MethodType.methodType(void.class, int.class, String.class));
        visitTypeInsn.invoke(this.methodVisitor, opcode, type);
        return this;
    }

    @SneakyThrows
    private MethodBuilder field(int opcode, String owner, String name, String descriptor) {
        MethodHandle visitFieldInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitFieldInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class));
        visitFieldInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor);
        return this;
    }

    @SneakyThrows
    private MethodBuilder method(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MethodHandle visitMethodInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMethodInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class, boolean.class));
        visitMethodInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor, isInterface);
        return this;
    }

    @SneakyThrows
    private MethodBuilder jump(int opcode, BytecodeLabel label) {
        MethodHandle visitJumpInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitJumpInsn", MethodType.methodType(void.class, int.class, CLASS_Label));
        visitJumpInsn.invoke(this.methodVisitor, opcode, label.getHandle());
        return this;
    }

}
