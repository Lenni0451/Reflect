package net.lenni0451.reflect.wrapper;

import lombok.SneakyThrows;
import net.lenni0451.reflect.ClassLoaders;
import net.lenni0451.reflect.stream.RStream;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

/**
 * A wrapper for the java internal ASM library.<br>
 * If ASM is present on the classpath it will be used instead.<br>
 * <b>This will probably stop working in the future, so don't rely on it...</b>
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public class ASMWrapper {

    private static final Class<?> CLASS_Opcodes;
    private static final Class<?> CLASS_ClassWriter;
    private static final Class<?> CLASS_FieldVisitor;
    private static final Class<?> CLASS_MethodVisitor;
    private static final Class<?> CLASS_Label;

    private static final Map<String, Integer> opcodes = new HashMap<>();

    static {
        CLASS_Opcodes = forName("org.objectweb.asm.Opcodes", "jdk.internal.org.objectweb.asm.Opcodes");
        CLASS_ClassWriter = forName("org.objectweb.asm.ClassWriter", "jdk.internal.org.objectweb.asm.ClassWriter");
        CLASS_FieldVisitor = forName("org.objectweb.asm.FieldVisitor", "jdk.internal.org.objectweb.asm.FieldVisitor");
        CLASS_MethodVisitor = forName("org.objectweb.asm.MethodVisitor", "jdk.internal.org.objectweb.asm.MethodVisitor");
        CLASS_Label = forName("org.objectweb.asm.Label", "jdk.internal.org.objectweb.asm.Label");

        RStream.of(CLASS_Opcodes).fields().filter(true).filter(int.class).forEach(f -> opcodes.put(f.name(), f.<Integer>get()));
    }

    private static Class<?> forName(final String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException("Could not find any of the classes: " + String.join(", ", names));
    }

    /**
     * Get an opcode by its name.
     *
     * @param name The name of the opcode
     * @return The opcode
     * @throws NullPointerException If the opcode does not exist
     */
    public static int opcode(final String name) {
        return opcodes.get(name);
    }

    /**
     * Replace all dots with slashes.
     *
     * @param className The class name
     * @return The class name with replaced dots
     */
    public static String slash(final String className) {
        return className.replace('.', '/');
    }

    /**
     * Get the name of the class with slashes instead of dots.
     *
     * @param clazz The class
     * @return The class name with slashes
     */
    public static String slash(final Class<?> clazz) {
        return slash(clazz.getName());
    }

    /**
     * Get a descriptor for the given class name.<br>
     * Dots in the class name will automatically be replaced with slashes.
     *
     * @param className The class name
     * @return The descriptor
     */
    public static String desc(final String className) {
        return "L" + slash(className) + ";";
    }

    /**
     * Get the descriptor for the given class.<br>
     * This also supports primitive types and arrays.
     *
     * @param clazz The class
     * @return The descriptor
     */
    public static String desc(final Class<?> clazz) {
        if (void.class.equals(clazz)) return "V";
        else if (boolean.class.equals(clazz)) return "Z";
        else if (byte.class.equals(clazz)) return "B";
        else if (short.class.equals(clazz)) return "S";
        else if (char.class.equals(clazz)) return "C";
        else if (int.class.equals(clazz)) return "I";
        else if (long.class.equals(clazz)) return "J";
        else if (float.class.equals(clazz)) return "F";
        else if (double.class.equals(clazz)) return "D";
        else if (clazz.isArray()) return "[" + desc(clazz.getComponentType());
        else return desc(clazz.getName());
    }

    /**
     * Get the descriptor for the given method.
     *
     * @param method The method
     * @return The descriptor
     */
    public static String desc(final Method method) {
        return desc(method.getParameterTypes(), method.getReturnType());
    }

    /**
     * Get the descriptor for the given parameter types and return type.
     *
     * @param parameterTypes The parameter types
     * @param returnType     The return type
     * @return The descriptor
     */
    public static String desc(final Class<?>[] parameterTypes, final Class<?> returnType) {
        StringBuilder builder = new StringBuilder("(");
        for (Class<?> parameterType : parameterTypes) builder.append(desc(parameterType));
        builder.append(")").append(desc(returnType));
        return builder.toString();
    }

    /**
     * Get the fitting return opcode for the given type.
     *
     * @param clazz The type
     * @return The opcode
     */
    public static int getLoadOpcode(final Class<?> clazz) {
        if (boolean.class.equals(clazz) || byte.class.equals(clazz) || char.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz)) return opcode("ILOAD");
        if (long.class.equals(clazz)) return opcode("LLOAD");
        if (float.class.equals(clazz)) return opcode("FLOAD");
        if (double.class.equals(clazz)) return opcode("DLOAD");
        return opcode("ALOAD");
    }

    /**
     * Get the fitting return opcode for the given type.<br>
     * {@link Void} will return {@code RETURN}.
     *
     * @param clazz The type
     * @return The opcode
     */
    public static int getReturnOpcode(final Class<?> clazz) {
        if (void.class.equals(clazz)) return opcode("RETURN");
        if (boolean.class.equals(clazz) || byte.class.equals(clazz) || char.class.equals(clazz) || short.class.equals(clazz) || int.class.equals(clazz)) return opcode("IRETURN");
        if (long.class.equals(clazz)) return opcode("LRETURN");
        if (float.class.equals(clazz)) return opcode("FRETURN");
        if (double.class.equals(clazz)) return opcode("DRETURN");
        return opcode("ARETURN");
    }

    /**
     * Create a new ASM wrapper with the given class properties.
     *
     * @param access     The access flags
     * @param name       The class name
     * @param signature  The signature
     * @param superName  The super class name
     * @param interfaces The interfaces
     * @return The ASM wrapper
     */
    public static ASMWrapper create(final int access, @Nonnull final String name, @Nullable final String signature, @Nonnull final String superName, @Nullable final String[] interfaces) {
        return new ASMWrapper(access, name, signature, superName, interfaces);
    }


    private final Object classWriter;

    private <E extends Throwable> ASMWrapper(final int access, final String name, final String signature, final String superName, final String[] interfaces) throws E {
        this.classWriter = RStream
                .of(CLASS_ClassWriter)
                .constructors()
                .by(int.class)
                .newInstance(2/*COMPUTE_FRAMES*/);

        try {
            MethodHandle visit = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visit", MethodType.methodType(void.class, int.class, int.class, String.class, String.class, String.class, String[].class));
            visit.invoke(this.classWriter, opcode("V1_8"), access, name, signature, superName, interfaces);
        } catch (Throwable t) {
            throw (E) t;
        }
    }

    /**
     * Visit a field in the class.
     *
     * @param access     The access flags
     * @param name       The field name
     * @param descriptor The field descriptor
     * @param signature  The field signature
     * @param value      The field value
     */
    @SneakyThrows
    public void visitField(final int access, @Nonnull final String name, @Nonnull final String descriptor, @Nullable final String signature, @Nullable final Object value) {
        MethodHandle visitField = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visitField", MethodType.methodType(CLASS_FieldVisitor, int.class, String.class, String.class, String.class, Object.class));
        MethodHandle visitEnd = TRUSTED_LOOKUP.findVirtual(CLASS_FieldVisitor, "visitEnd", MethodType.methodType(void.class));

        Object fieldVisitor = visitField.invoke(this.classWriter, access, name, descriptor, signature, value);
        visitEnd.invoke(fieldVisitor);
    }

    /**
     * Visit a method in the class.
     *
     * @param access     The access flags
     * @param name       The method name
     * @param descriptor The method descriptor
     * @param signature  The method signature
     * @param exceptions The exceptions
     * @return The method visitor
     */
    @SneakyThrows
    public MethodVisitorAccess visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        MethodHandle visitMethod = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visitMethod", MethodType.methodType(CLASS_MethodVisitor, int.class, String.class, String.class, String.class, String[].class));
        MethodHandle visitCode = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitCode", MethodType.methodType(void.class));

        Object methodVisitor = visitMethod.invoke(this.classWriter, access, name, descriptor, signature, exceptions);
        visitCode.invoke(methodVisitor);
        return new MethodVisitorAccess(methodVisitor);
    }

    /**
     * Get a new label.
     *
     * @return The label
     */
    @SneakyThrows
    public LabelAccess label() {
        MethodHandle constructor = TRUSTED_LOOKUP.findConstructor(CLASS_Label, MethodType.methodType(void.class));
        return new LabelAccess(constructor.invoke());
    }

    /**
     * Get the byte array of the generated class.
     *
     * @return The byte array
     */
    @SneakyThrows
    public byte[] toByteArray() {
        MethodHandle toByteArray = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "toByteArray", MethodType.methodType(byte[].class));
        return (byte[]) toByteArray.invoke(this.classWriter);
    }

    /**
     * Define the class anonymously.
     *
     * @param parent The parent class
     * @return The defined class
     */
    public Class<?> defineAnonymously(final Class<?> parent) {
        return ClassLoaders.defineAnonymousClass(parent, this.toByteArray());
    }

    /**
     * Define the class as a strong nestmate (like the {@link LambdaMetafactory}).
     *
     * @param parent The parent class
     * @return The defined class
     */
    public Class<?> defineMetafactory(final Class<?> parent) {
        return ClassLoaders.defineAnonymousClass(parent, this.toByteArray(), "NESTMATE", "STRONG");
    }


    /**
     * A wrapper for the ASM method visitor.
     */
    public static class MethodVisitorAccess {
        private final Object methodVisitor;

        private MethodVisitorAccess(final Object methodVisitor) {
            this.methodVisitor = methodVisitor;
        }

        /**
         * Check if the method visitor is null.
         *
         * @return If the method visitor is null
         */
        public boolean isNull() {
            return this.methodVisitor == null;
        }

        /**
         * Visit an insn node.
         *
         * @param opcode The opcode
         */
        @SneakyThrows
        public void visitInsn(final int opcode) {
            MethodHandle visitInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitInsn", MethodType.methodType(void.class, int.class));
            visitInsn.invoke(this.methodVisitor, opcode);
        }

        /**
         * Visit an int insn node.
         *
         * @param opcode  The opcode
         * @param operand The operand
         */
        @SneakyThrows
        public void visitIntInsn(final int opcode, final int operand) {
            MethodHandle visitIntInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIntInsn", MethodType.methodType(void.class, int.class, int.class));
            visitIntInsn.invoke(this.methodVisitor, opcode, operand);
        }

        /**
         * Visit a var insn node.
         *
         * @param opcode   The opcode
         * @param varIndex The var index
         */
        @SneakyThrows
        public void visitVarInsn(final int opcode, final int varIndex) {
            MethodHandle visitVarInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitVarInsn", MethodType.methodType(void.class, int.class, int.class));
            visitVarInsn.invoke(this.methodVisitor, opcode, varIndex);
        }

        /**
         * Visit a type insn node.
         *
         * @param opcode The opcode
         * @param type   The type
         */
        @SneakyThrows
        public void visitTypeInsn(final int opcode, final String type) {
            MethodHandle visitTypeInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTypeInsn", MethodType.methodType(void.class, int.class, String.class));
            visitTypeInsn.invoke(this.methodVisitor, opcode, type);
        }

        /**
         * Visit a field insn node.
         *
         * @param opcode     The opcode
         * @param owner      The owner
         * @param name       The name
         * @param descriptor The descriptor
         */
        @SneakyThrows
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
            MethodHandle visitFieldInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitFieldInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class));
            visitFieldInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor);
        }

        /**
         * Visit a method insn node.
         *
         * @param opcode      The opcode
         * @param owner       The owner
         * @param name        The name
         * @param descriptor  The descriptor
         * @param isInterface Whether the method is an interface method
         */
        @SneakyThrows
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            MethodHandle visitMethodInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMethodInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class, boolean.class));
            visitMethodInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor, isInterface);
        }

        /**
         * Visit a jump insn node.
         *
         * @param opcode The opcode
         * @param target The target label
         */
        @SneakyThrows
        public void visitJumpInsn(final int opcode, final LabelAccess target) {
            MethodHandle visitJumpInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitJumpInsn", MethodType.methodType(void.class, int.class, CLASS_Label));
            visitJumpInsn.invoke(this.methodVisitor, opcode, target.label);
        }

        /**
         * Visit a label.
         *
         * @param label The label
         */
        @SneakyThrows
        public void visitLabel(final LabelAccess label) {
            MethodHandle visitLabel = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitLabel", MethodType.methodType(void.class, CLASS_Label));
            visitLabel.invoke(this.methodVisitor, label.label);
        }

        /**
         * Visit a ldc insn node.
         *
         * @param value The value
         */
        @SneakyThrows
        public void visitLdcInsn(final Object value) {
            MethodHandle visitLdcInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitLdcInsn", MethodType.methodType(void.class, Object.class));
            visitLdcInsn.invoke(this.methodVisitor, value);
        }

        /**
         * Visit an iinc insn node.
         *
         * @param varIndex  The var index
         * @param increment The increment
         */
        @SneakyThrows
        public void visitIincInsn(final int varIndex, final int increment) {
            MethodHandle visitIincInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIincInsn", MethodType.methodType(void.class, int.class, int.class));
            visitIincInsn.invoke(this.methodVisitor, varIndex, increment);
        }

        /**
         * Visit a multi a new array insn.
         *
         * @param descriptor    The descriptor
         * @param numDimensions The number of dimensions
         */
        @SneakyThrows
        public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
            MethodHandle visitMultiANewArrayInsn = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMultiANewArrayInsn", MethodType.methodType(void.class, String.class, int.class));
            visitMultiANewArrayInsn.invoke(this.methodVisitor, descriptor, numDimensions);
        }

        /**
         * Visit a try catch block.
         *
         * @param start   The start label
         * @param end     The end label
         * @param handler The handler label
         * @param type    The type
         */
        @SneakyThrows
        public void visitTryCatchBlock(final LabelAccess start, final LabelAccess end, final LabelAccess handler, final String type) {
            MethodHandle visitTryCatchBlock = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTryCatchBlock", MethodType.methodType(void.class, CLASS_Label, CLASS_Label, CLASS_Label, String.class));
            visitTryCatchBlock.invoke(this.methodVisitor, start.label, end.label, handler.label, type);
        }

        /**
         * Visit the method maxs.
         *
         * @param maxStack  The max stack
         * @param maxLocals The max locals
         */
        @SneakyThrows
        public void visitMaxs(final int maxStack, final int maxLocals) {
            MethodHandle visitMaxs = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMaxs", MethodType.methodType(void.class, int.class, int.class));
            visitMaxs.invoke(this.methodVisitor, maxStack, maxLocals);
        }

        /**
         * Visit the method end.
         */
        @SneakyThrows
        public void visitEnd() {
            MethodHandle visitEnd = TRUSTED_LOOKUP.findVirtual(CLASS_MethodVisitor, "visitEnd", MethodType.methodType(void.class));
            visitEnd.invoke(this.methodVisitor);
        }
    }

    /**
     * A wrapper for the ASM label.
     */
    public static class LabelAccess {
        private final Object label;

        private LabelAccess(final Object label) {
            this.label = label;
        }

        /**
         * Check if the label is null.
         *
         * @return If the label is null
         */
        public boolean isNull() {
            return this.label == null;
        }
    }

}
