package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static net.lenni0451.reflect.JVMConstants.CLASS_InstrumentationImpl;
import static net.lenni0451.reflect.JVMConstants.METHOD_InstrumentationImpl_loadAgent;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;
import static net.lenni0451.reflect.utils.FieldInitializer.optInit;

/**
 * This class contains some useful methods for working with agents.
 */
public class Agents {

    private static final String DUMMY_AGENT_CLASS_NAME = String.join(".", "net", "lenni0451", "reflect", "AgentLoader"); //Prevent repackaging tools from changing the name
    private static final String INSTRUMENTATION_FIELD_NAME = "instrumentation";
    private static final Class<?> instrumentationImpl = optInit(
            () -> Class.forName(CLASS_InstrumentationImpl)
    );
    private static final MethodHandle loadAgent = optInit(
            () -> Methods.getDeclaredMethod(instrumentationImpl, METHOD_InstrumentationImpl_loadAgent, String.class),
            TRUSTED_LOOKUP::unreflect
    );

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
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(agentJar))) {
            jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

            Manifest manifest = new Manifest();
            manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
            manifest.getMainAttributes().putValue("Launcher-Agent-Class", agentName);
            manifest.getMainAttributes().putValue("Can-Redefine-Classes", "true");
            manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
            manifest.getMainAttributes().putValue("Can-Set-Native-Method-Prefix", "true");
            manifest.write(jos);
        }
    }

    /**
     * Load an agent from a jar during runtime.<br>
     * The method used was added in Java 9 and is not available in Java 8.
     *
     * @param agentJar The path to the agent jar
     * @throws IllegalStateException If the loadAgent method does not exist
     */
    @SneakyThrows
    public static void load(final File agentJar) {
        if (loadAgent == null) {
            throw new IllegalStateException("Loading an Agent during runtime is not possible because the " + METHOD_InstrumentationImpl_loadAgent + " method does not exist");
        }
        loadAgent.invokeExact(agentJar.getAbsolutePath());
    }

    /**
     * Create a dummy agent jar for the given class and load it.<br>
     * The method used was added in Java 9 and is not available in Java 8.
     *
     * @param agentClass The class to create the agent for
     * @throws IOException           If an I/O error occurs
     * @throws IllegalStateException If the loadAgent method does not exist
     */
    public static void loadInternal(final Class<?> agentClass) throws IOException {
        load(createDummyAgent(agentClass));
    }

    /**
     * Get an instrumentation instance by loading an agent during runtime.<br>
     * The loaded agent stores the instrumentation instance in a static field which is accessed using reflection.<br>
     * This solves many problems which can occur with class loaders.<br>
     * The instrumentation instance is cached so that it is only loaded once.
     *
     * @return The instrumentation instance
     * @throws IOException           If an IO error occurs
     * @throws IllegalStateException If the loadAgent method does not exist
     */
    public static Instrumentation getInstrumentation() throws IOException {
        Class<?> agentLoaderClass;
        try {
            agentLoaderClass = ClassLoader.getSystemClassLoader().loadClass(DUMMY_AGENT_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            //Load the agent loader into the system class loader
            agentLoaderClass = ClassLoaders.defineClass(ClassLoader.getSystemClassLoader(), DUMMY_AGENT_CLASS_NAME, generateAgentClass());

            load(createDummyAgent(agentLoaderClass));
        }

        return Fields.get(null, Fields.getDeclaredField(agentLoaderClass, "instrumentation"));
    }

    private static byte[] generateAgentClass() {
        BytecodeBuilder builder = BytecodeBuilder.get();
        BuiltClass clazz = builder.class_(builder.opcode("ACC_PUBLIC"), slash(DUMMY_AGENT_CLASS_NAME), null, slash(Object.class), null, cb -> {
            cb.field(builder.opcode("ACC_PUBLIC", "ACC_STATIC"), INSTRUMENTATION_FIELD_NAME, desc(Instrumentation.class), null, null);
            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", "()V", null, null, mb -> mb
                    .aload(0)
                    .invokespecial(slash(Object.class), "<init>", "()V", false)
                    .return_()
            );
            for (String methodName : new String[]{"agentmain", "premain"}) {
                cb.method(builder.opcode("ACC_PUBLIC", "ACC_STATIC"), methodName, mdesc(void.class, String.class, Instrumentation.class), null, null, mb -> mb
                        .aload(1)
                        .putstatic(slash(DUMMY_AGENT_CLASS_NAME), INSTRUMENTATION_FIELD_NAME, desc(Instrumentation.class))
                        .return_()
                );
            }
        });
        return clazz.toBytes();
    }

}
