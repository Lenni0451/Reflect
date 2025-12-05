package net.lenni0451.reflect;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
     * @throws IllegalStateException If the agent could not be loaded for any reason
     */
    @SneakyThrows
    public static void load(final File agentJar) {
        if (loadAgent == null) {
            throw new IllegalStateException("Loading an Agent during runtime is not possible because the " + METHOD_InstrumentationImpl_loadAgent + " method does not exist");
        }
        try {
            loadAgent.invokeExact(agentJar.getAbsolutePath());
        } catch (InternalError e) {
            //The JVM throws an InternalError if anything goes wrong while loading the agent
            //Sadly there are multiple reasons why this can happen and all of them lead to the same exception
            //On Windows this can happen if the path or name of the jar contains any non-ascii characters
            //This code attempts to copy the agent to the run directory with a simple name and load it from there
            File simpleAgentJar = new File(".reflect_temp_agent" + System.nanoTime() + ".jar");
            try {
                Files.copy(agentJar.toPath(), simpleAgentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //Try to load the agent by name only
                loadAgent.invokeExact(simpleAgentJar.getName());
                //If no exception was thrown the agent was loaded successfully
            } catch (Throwable t) {
                //If it still fails, throw the original exception and add some context
                IllegalStateException finalException = new IllegalStateException("Failed to load agent from jar: " + agentJar.getAbsolutePath(), e);
                finalException.addSuppressed(t);
                throw finalException;
            } finally {
                //Try to delete the simple agent jar immediately
                //It seems to not be locked after loading, but just to be sure it is also marked for deletion on exit
                simpleAgentJar.deleteOnExit();
                simpleAgentJar.delete();
            }
        }
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
     * @throws IllegalStateException If the agent could not be loaded for any reason
     */
    public static Instrumentation getInstrumentation() throws IOException {
        ClassLoader targetClassLoader = ClassLoader.getSystemClassLoader();
        Class<?> agentLoaderClass;
        try {
            agentLoaderClass = targetClassLoader.loadClass(DUMMY_AGENT_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            //Load the agent loader into the system class loader
            agentLoaderClass = ClassLoaders.defineClass(targetClassLoader, DUMMY_AGENT_CLASS_NAME, generateAgentClass());

            load(createDummyAgent(agentLoaderClass));
        }

        if (agentLoaderClass == null) {
            throw new IllegalStateException("Agent loader class is null", new ClassNotFoundException(DUMMY_AGENT_CLASS_NAME));
        }
        Field field = Fields.getDeclaredField(agentLoaderClass, INSTRUMENTATION_FIELD_NAME);
        if (field == null) {
            throw new IllegalStateException("Instrumentation field not found in agent loader class", new FieldNotFoundException(agentLoaderClass.getName(), INSTRUMENTATION_FIELD_NAME));
        }
        Instrumentation instrumentation = Fields.get(null, field);
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation instance in class " + agentLoaderClass.getName() + " in loader " + targetClassLoader + " is null");
        }
        return instrumentation;
    }

    private static byte[] generateAgentClass() {
        BytecodeBuilder builder = BytecodeBuilder.get();
        BuiltClass clazz = builder.class_(builder.opcode("ACC_PUBLIC"), slash(DUMMY_AGENT_CLASS_NAME), null, slash(Object.class), null, cb -> {
            cb.field(builder.opcode("ACC_PUBLIC", "ACC_STATIC"), INSTRUMENTATION_FIELD_NAME, desc(Instrumentation.class), null, null);
            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", "()V", null, null, mb -> mb
                    .aload(0)
                    .invokespecial(slash(Object.class), "<init>", "()V", false)
                    .return_()
                    .maxs(1, 1)
            );
            for (String methodName : new String[]{"agentmain", "premain"}) {
                cb.method(builder.opcode("ACC_PUBLIC", "ACC_STATIC"), methodName, mdesc(void.class, String.class, Instrumentation.class), null, null, mb -> mb
                        .aload(1)
                        .putstatic(slash(DUMMY_AGENT_CLASS_NAME), INSTRUMENTATION_FIELD_NAME, desc(Instrumentation.class))
                        .return_()
                        .maxs(1, 2)
                );
            }
        });
        return clazz.toBytes();
    }

}
