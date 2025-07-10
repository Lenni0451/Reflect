package net.lenni0451.reflect.bytecode.impl.asm;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.MethodBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.CLASS_Label;
import static net.lenni0451.reflect.bytecode.impl.asm.ASMBuilder.CLASS_MethodVisitor;

@ApiStatus.Internal
class ASMMethodBuilder implements MethodBuilder {

    private final Object methodVisitor;

    public ASMMethodBuilder(final Object methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    public Object getMethodVisitor() {
        return this.methodVisitor;
    }

    @Override
    @SneakyThrows
    public MethodBuilder insn(int opcode) {
        MethodHandle visitInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitInsn", MethodType.methodType(void.class, int.class));
        visitInsn.invoke(this.methodVisitor, opcode);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder int_(int opcode, int value) {
        MethodHandle visitIntInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIntInsn", MethodType.methodType(void.class, int.class, int.class));
        visitIntInsn.invoke(this.methodVisitor, opcode, value);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder var(int opcode, int varIndex) {
        MethodHandle visitVarInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitVarInsn", MethodType.methodType(void.class, int.class, int.class));
        visitVarInsn.invoke(this.methodVisitor, opcode, varIndex);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder type(int opcode, String type) {
        MethodHandle visitTypeInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTypeInsn", MethodType.methodType(void.class, int.class, String.class));
        visitTypeInsn.invoke(this.methodVisitor, opcode, type);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder field(int opcode, String owner, String name, String descriptor) {
        MethodHandle visitFieldInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitFieldInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class));
        visitFieldInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder method(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MethodHandle visitMethodInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMethodInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class, boolean.class));
        visitMethodInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor, isInterface);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder jump(int opcode, BytecodeLabel label) {
        MethodHandle visitJumpInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitJumpInsn", MethodType.methodType(void.class, int.class, CLASS_Label));
        visitJumpInsn.invoke(this.methodVisitor, opcode, label.getHandle());
        return this;
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
    public MethodBuilder iinc(int varIndex, int increment) {
        MethodHandle visitIincInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIincInsn", MethodType.methodType(void.class, int.class, int.class));
        visitIincInsn.invoke(this.methodVisitor, varIndex, increment);
        return this;
    }

    @Override
    @SneakyThrows
    public MethodBuilder multiANewArray(String descriptor, int dimensions) {
        MethodHandle visitMultiANewArrayInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMultiANewArrayInsn", MethodType.methodType(void.class, String.class, int.class));
        visitMultiANewArrayInsn.invoke(this.methodVisitor, descriptor, dimensions);
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

}
