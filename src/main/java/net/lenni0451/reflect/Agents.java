package net.lenni0451.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * This class contains some useful methods for working with agents.
 */
public class Agents {

    /*
        ClassNode node = new ClassNode();
        node.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "net/lenni0451/reflect/AgentLoader", null, "java/lang/Object", null);

        node.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "instrumentation", "Ljava/lang/instrument/Instrumentation;", null, null).visitEnd();
        { //<init>
            MethodVisitor method = node.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            method.visitInsn(Opcodes.RETURN);
            method.visitEnd();
        }
        { //agentmain
            MethodVisitor method = node.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "agentmain", "(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 1);
            method.visitFieldInsn(Opcodes.PUTSTATIC, node.name, "instrumentation", "Ljava/lang/instrument/Instrumentation;");
            method.visitInsn(Opcodes.RETURN);
            method.visitEnd();
        }
        { //premain
            MethodVisitor method = node.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "premain", "(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 1);
            method.visitFieldInsn(Opcodes.PUTSTATIC, node.name, "instrumentation", "Ljava/lang/instrument/Instrumentation;");
            method.visitInsn(Opcodes.RETURN);
            method.visitEnd();
        }

        node.visitEnd();
        byte[] bytes = ASMUtils.toBytes(node, new BasicClassProvider());
        Files.write(new File("agentloader.bin").toPath(), bytes);
     */
    private static final String DUMMY_AGENT_CLASS = "yv66vgAAADQAEQEAIW5ldC9sZW5uaTA0NTEvcmVmbGVjdC9BZ2VudExvYWRlcgcAAQEAEGphdmEvbGFuZy9PYmplY3QHAAMBAA9p" +
            "bnN0cnVtZW50YXRpb24BACZMamF2YS9sYW5nL2luc3RydW1lbnQvSW5zdHJ1bWVudGF0aW9uOwEABjxpbml0PgEAAygpVgwABwAI" +
            "CgAEAAkBAAlhZ2VudG1haW4BADsoTGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9pbnN0cnVtZW50L0luc3RydW1lbnRhdGlv" +
            "bjspVgwABQAGCQACAA0BAAdwcmVtYWluAQAEQ29kZQABAAIABAAAAAEACQAFAAYAAAADAAEABwAIAAEAEAAAABEAAQABAAAABSq3" +
            "AAqxAAAAAAAJAAsADAABABAAAAARAAEAAgAAAAUrswAOsQAAAAAACQAPAAwAAQAQAAAAEQABAAIAAAAFK7MADrEAAAAAAAA=";

    /**
     * Create a temp empty dummy agent jar for a given class.
     *
     * @param agentClass The class to create the agent for
     * @return The path to the agent jar
     * @throws IOException If an I/O error occurs
     */
    public static File createDummyAgent(final Class<?> agentClass) throws IOException {
        File agentJar = File.createTempFile("DummyAgent", ".jar");
        agentJar.deleteOnExit();
        createDummyAgent(agentJar, agentClass.getName());
        return agentJar;
    }

    /**
     * Create an empty dummy agent jar for a given class.
     *
     * @param agentJar  The path to the agent jar
     * @param agentName The name of the agent class
     * @throws IOException If an I/O error occurs
     */
    public static void createDummyAgent(final File agentJar, final String agentName) throws IOException {
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(agentJar));
        jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Launcher-Agent-Class", agentName);
        manifest.getMainAttributes().putValue("Can-Redefine-Classes", "true");
        manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
        manifest.write(jos);

        jos.closeEntry();
        jos.close();
    }

    /**
     * Load an agent from a jar during runtime.<br>
     * The method used was added in Java 9 and is not available in Java 8.
     *
     * @param agentJar The path to the agent jar
     * @throws ClassNotFoundException If the InstrumentationImpl class is not found
     * @throws IllegalStateException  If the loadAgent method does not exist
     */
    public static void load(final File agentJar) throws ClassNotFoundException {
        Class<?> instrumentationImpl = Class.forName("sun.instrument.InstrumentationImpl");
        Method method = Methods.getDeclaredMethod(instrumentationImpl, "loadAgent", String.class);
        if (method == null) throw new IllegalStateException("Loading an Agent during runtime is not possible because loadAgent method does not exist");
        Methods.invoke(null, method, agentJar.getAbsolutePath());
    }

    /**
     * Create a dummy agent jar for the given class and load it.<br>
     * The method used was added in Java 9 and is not available in Java 8.
     *
     * @param agentClass The class to create the agent for
     * @throws IOException            If an I/O error occurs
     * @throws ClassNotFoundException If the InstrumentationImpl class is not found
     * @throws IllegalStateException  If the loadAgent method does not exist
     */
    public static void loadInternal(final Class<?> agentClass) throws IOException, ClassNotFoundException {
        load(createDummyAgent(agentClass));
    }

    /**
     * Get an instrumentation instance by loading an agent during runtime.<br>
     * The loaded agent stores the instrumentation instance in a static field which is accessed using reflection.<br>
     * This solves many problems which can occur with class loaders.<br>
     * The instrumentation instance is cached so that it is only loaded once.
     *
     * @return The instrumentation instance
     * @throws IOException            If an IO error occurs
     * @throws ClassNotFoundException If the InstrumentationImpl class is not found
     * @throws IllegalStateException  If the loadAgent method does not exist
     */
    public static Instrumentation getInstrumentation() throws IOException, ClassNotFoundException {
        final String agentLoaderClassName = "net.lenni0451.reflect.AgentLoader";

        Class<?> agentLoaderClass;
        try {
            agentLoaderClass = ClassLoader.getSystemClassLoader().loadClass(agentLoaderClassName);
        } catch (ClassNotFoundException e) {
            //Load the agent loader into the system class loader
            agentLoaderClass = ClassLoaders.defineClass(ClassLoader.getSystemClassLoader(), agentLoaderClassName, Base64.getDecoder().decode(DUMMY_AGENT_CLASS));

            load(createDummyAgent(agentLoaderClass));
        }

        return Fields.get(null, Fields.getDeclaredField(agentLoaderClass, "instrumentation"));
    }

}
