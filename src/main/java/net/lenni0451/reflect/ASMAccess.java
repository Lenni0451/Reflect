package net.lenni0451.reflect;

import net.lenni0451.reflect.stream.RStream;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

/**
 * A wrapper for the java internal ASM library.<br>
 * If ASM is present on the classpath it will be used instead.
 */
@SuppressWarnings("unchecked")
public class ASMAccess {

    private static final Unsafe UNSAFE = JavaBypass.UNSAFE;
    private static final MethodHandles.Lookup LOOKUP = JavaBypass.TRUSTED_LOOKUP;
    private static final Class<?> CLASS_Opcodes;
    private static final Class<?> CLASS_ClassWriter;
    private static final Class<?> CLASS_FieldVisitor;
    private static final Class<?> CLASS_MethodVisitor;

    private static final Map<String, Integer> opcodes = new HashMap<>();

    static {
        Modules.copyModule(System.class, ASMAccess.class);
        Modules.copyModule(System.class, MethodVisitorAccess.class);

        CLASS_Opcodes = forName("org.objectweb.asm.Opcodes", "jdk.internal.org.objectweb.asm.Opcodes");
        CLASS_ClassWriter = forName("org.objectweb.asm.ClassWriter", "jdk.internal.org.objectweb.asm.ClassWriter");
        CLASS_FieldVisitor = forName("org.objectweb.asm.FieldVisitor", "jdk.internal.org.objectweb.asm.FieldVisitor");
        CLASS_MethodVisitor = forName("org.objectweb.asm.MethodVisitor", "jdk.internal.org.objectweb.asm.MethodVisitor");

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

    public static int opcode(final String name) {
        return opcodes.get(name);
    }

    public static String dash(final String className) {
        return className.replace('.', '/');
    }

    public static String dash(final Class<?> clazz) {
        return dash(clazz.getName());
    }

    public static String desc(final String className) {
        return "L" + dash(className) + ";";
    }

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

    public static ASMAccess create(final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        return new ASMAccess(access, name, signature, superName, interfaces);
    }


    private final Object classWriter;

    private <E extends Throwable> ASMAccess(final int access, final String name, final String signature, final String superName, final String[] interfaces) throws E {
        this.classWriter = RStream
                .of(CLASS_ClassWriter)
                .constructors()
                .by(int.class)
                .newInstance(2);

        try {
            MethodHandle visit = LOOKUP.findVirtual(CLASS_ClassWriter, "visit", MethodType.methodType(void.class, int.class, int.class, String.class, String.class, String.class, String[].class));
            visit.invoke(this.classWriter, opcode("V1_8"), access, name, signature, superName, interfaces);
        } catch (Throwable t) {
            throw (E) t;
        }
    }

    public <E extends Throwable> void visitField(final int access, final String name, final String descriptor, final String signature, final Object value) throws E {
        try {
            MethodHandle visitField = LOOKUP.findVirtual(CLASS_ClassWriter, "visitField", MethodType.methodType(CLASS_FieldVisitor, int.class, String.class, String.class, String.class, Object.class));
            MethodHandle visitEnd = LOOKUP.findVirtual(CLASS_FieldVisitor, "visitEnd", MethodType.methodType(void.class));

            Object fieldVisitor = visitField.invoke(this.classWriter, access, name, descriptor, signature, value);
            visitEnd.invoke(fieldVisitor);
        } catch (Throwable t) {
            throw (E) t;
        }
    }

    public <E extends Throwable> MethodVisitorAccess visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) throws E {
        try {
            MethodHandle visitMethod = LOOKUP.findVirtual(CLASS_ClassWriter, "visitMethod", MethodType.methodType(CLASS_MethodVisitor, int.class, String.class, String.class, String.class, String[].class));
            MethodHandle visitCode = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitCode", MethodType.methodType(void.class));

            Object methodVisitor = visitMethod.invoke(this.classWriter, access, name, descriptor, signature, exceptions);
            visitCode.invoke(methodVisitor);
            return new MethodVisitorAccess(methodVisitor);
        } catch (Throwable t) {
            throw (E) t;
        }
    }

    public <E extends Throwable> byte[] toByteArray() throws E {
        try {
            MethodHandle toByteArray = LOOKUP.findVirtual(CLASS_ClassWriter, "toByteArray", MethodType.methodType(byte[].class));
            return (byte[]) toByteArray.invoke(this.classWriter);
        } catch (Throwable t) {
            throw (E) t;
        }
    }

    public Class<?> defineAnonymously(final Class<?> parent) {
        return ClassLoaders.defineAnonymousClass(parent, this.toByteArray());
    }

    public Class<?> defineMetafactory(final Class<?> parent) {
        Method unsafeDefineAnonymousClass = Methods.getDeclaredMethod(Unsafe.class, "defineAnonymousClass", Class.class, byte[].class, Object[].class);
        if (unsafeDefineAnonymousClass != null) return Methods.invoke(UNSAFE, unsafeDefineAnonymousClass, parent, this.toByteArray(), new Object[0]);

        Class<?> classOptionClass = Classes.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
        Object classOptionArray = Array.newInstance(classOptionClass, 2);
        Array.set(classOptionArray, 0, classOptionClass.getEnumConstants()[0]);
        Array.set(classOptionArray, 1, classOptionClass.getEnumConstants()[1]);
        Method lookupDefineHiddenClass = Methods.getDeclaredMethod(MethodHandles.Lookup.class, "defineHiddenClass", byte[].class, boolean.class, classOptionArray.getClass());
        MethodHandles.Lookup lookup = Methods.invoke(TRUSTED_LOOKUP.in(parent), lookupDefineHiddenClass, toByteArray(), false, classOptionArray);
        return lookup.lookupClass();
    }


    public static class MethodVisitorAccess {
        private final Object methodVisitor;

        private MethodVisitorAccess(final Object methodVisitor) {
            this.methodVisitor = methodVisitor;
        }

        public <E extends Throwable> void visitInsn(final int opcode) throws E {
            try {
                MethodHandle visitInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitInsn", MethodType.methodType(void.class, int.class));
                visitInsn.invoke(this.methodVisitor, opcode);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitIntInsn(final int opcode, final int operand) throws E {
            try {
                MethodHandle visitIntInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIntInsn", MethodType.methodType(void.class, int.class, int.class));
                visitIntInsn.invoke(this.methodVisitor, opcode, operand);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitVarInsn(final int opcode, final int varIndex) throws E {
            try {
                MethodHandle visitVarInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitVarInsn", MethodType.methodType(void.class, int.class, int.class));
                visitVarInsn.invoke(this.methodVisitor, opcode, varIndex);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitTypeInsn(final int opcode, final String type) throws E {
            try {
                MethodHandle visitTypeInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitTypeInsn", MethodType.methodType(void.class, int.class, String.class));
                visitTypeInsn.invoke(this.methodVisitor, opcode, type);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) throws E {
            try {
                MethodHandle visitFieldInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitFieldInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class));
                visitFieldInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) throws E {
            try {
                MethodHandle visitMethodInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMethodInsn", MethodType.methodType(void.class, int.class, String.class, String.class, String.class, boolean.class));
                visitMethodInsn.invoke(this.methodVisitor, opcode, owner, name, descriptor, isInterface);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitLdcInsn(final Object value) throws E {
            try {
                MethodHandle visitLdcInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitLdcInsn", MethodType.methodType(void.class, Object.class));
                visitLdcInsn.invoke(this.methodVisitor, value);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitIincInsn(final int varIndex, final int increment) throws E {
            try {
                MethodHandle visitIincInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitIincInsn", MethodType.methodType(void.class, int.class, int.class));
                visitIincInsn.invoke(this.methodVisitor, varIndex, increment);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) throws E {
            try {
                MethodHandle visitMultiANewArrayInsn = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMultiANewArrayInsn", MethodType.methodType(void.class, String.class, int.class));
                visitMultiANewArrayInsn.invoke(this.methodVisitor, descriptor, numDimensions);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitMaxs(final int maxStack, final int maxLocals) throws E {
            try {
                MethodHandle visitMaxs = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitMaxs", MethodType.methodType(void.class, int.class, int.class));
                visitMaxs.invoke(this.methodVisitor, maxStack, maxLocals);
            } catch (Throwable t) {
                throw (E) t;
            }
        }

        public <E extends Throwable> void visitEnd() throws E {
            try {
                MethodHandle visitEnd = LOOKUP.findVirtual(CLASS_MethodVisitor, "visitEnd", MethodType.methodType(void.class));
                visitEnd.invoke(this.methodVisitor);
            } catch (Throwable t) {
                throw (E) t;
            }
        }
    }

}
