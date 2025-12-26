package net.lenni0451.reflect;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.net.URL;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ClassLoadersTest {

    private static byte[] testClassBytes;

    @BeforeAll
    static void setUp() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "net/lenni0451/reflect/ASMTestClass", null, "java/lang/Object", new String[]{"java/util/function/Supplier"});

        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(1, 1);
        c.visitEnd();

        MethodVisitor s = cw.visitMethod(Opcodes.ACC_PUBLIC, "get", "()Ljava/lang/Object;", null, null);
        s.visitCode();
        s.visitLdcInsn("Hello World");
        s.visitInsn(Opcodes.ARETURN);
        s.visitMaxs(1, 1);
        s.visitEnd();

        cw.visitEnd();
        testClassBytes = cw.toByteArray();
    }

    @Test
    void getSystemClassPath() {
        URL[] systemClassPath = assertDoesNotThrow(ClassLoaders::getSystemClassPath);
        assertNotNull(systemClassPath);
        assertTrue(systemClassPath.length > 0);
    }

    @Test
    void defineClass() {
        ClassLoader classLoader = ClassLoadersTest.class.getClassLoader();
        Class<?> supplier = assertDoesNotThrow(() -> ClassLoaders.defineClass(classLoader, null, testClassBytes));
        Supplier<String> instance = assertDoesNotThrow(() -> (Supplier<String>) supplier.getDeclaredConstructor().newInstance());
        String response = assertDoesNotThrow(instance::get);
        assertEquals("Hello World", response);
        assertEquals(classLoader, supplier.getClassLoader());
    }

    @Test
    void defineClassOnBootstrapClassLoader() {
        ClassLoader bootstrapClassLoader = System.class.getClassLoader(); // Typically null, but implementation-dependant
        Class<?> supplier = assertDoesNotThrow(() -> ClassLoaders.defineClass(bootstrapClassLoader, null, testClassBytes));
        Supplier<String> instance = assertDoesNotThrow(() -> (Supplier<String>) supplier.getDeclaredConstructor().newInstance());
        String response = assertDoesNotThrow(instance::get);
        assertEquals("Hello World", response);
        assertEquals(bootstrapClassLoader, supplier.getClassLoader());
    }

    @Test
    void defineAnonymousClass() {
        Class<?> supplier = assertDoesNotThrow(() -> ClassLoaders.defineAnonymousClass(ClassLoadersTest.class, testClassBytes));
        Supplier<String> instance = assertDoesNotThrow(() -> (Supplier<String>) supplier.getDeclaredConstructor().newInstance());
        String response = assertDoesNotThrow(instance::get);
        assertEquals("Hello World", response);
    }

}
