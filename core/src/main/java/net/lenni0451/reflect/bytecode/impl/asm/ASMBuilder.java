package net.lenni0451.reflect.bytecode.impl.asm;

import lombok.SneakyThrows;
import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import net.lenni0451.reflect.bytecode.wrapper.BytecodeType;
import net.lenni0451.reflect.stream.RStream;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

@ApiStatus.Internal
class ASMBuilder implements BytecodeBuilder {

    public static final Class<?> CLASS_Opcodes;
    public static final Class<?> CLASS_ClassWriter;
    public static final Class<?> CLASS_FieldVisitor;
    public static final Class<?> CLASS_MethodVisitor;
    public static final Class<?> CLASS_Label;
    public static final Class<?> CLASS_type;

    private static final Map<String, Integer> opcodes = new HashMap<>();

    static {
        CLASS_Opcodes = forName("org.objectweb.asm.Opcodes", "jdk.internal.org.objectweb.asm.Opcodes");
        CLASS_ClassWriter = forName("org.objectweb.asm.ClassWriter", "jdk.internal.org.objectweb.asm.ClassWriter");
        CLASS_FieldVisitor = forName("org.objectweb.asm.FieldVisitor", "jdk.internal.org.objectweb.asm.FieldVisitor");
        CLASS_MethodVisitor = forName("org.objectweb.asm.MethodVisitor", "jdk.internal.org.objectweb.asm.MethodVisitor");
        CLASS_Label = forName("org.objectweb.asm.Label", "jdk.internal.org.objectweb.asm.Label");
        CLASS_type = forName("org.objectweb.asm.Type", "jdk.internal.org.objectweb.asm.Type");

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


    @Override
    @SneakyThrows
    public BuiltClass class_(int access, String name, String signature, String superName, String[] interfaces, Consumer<ClassBuilder> consumer) {
        MethodHandle constructor = TRUSTED_LOOKUP.findConstructor(CLASS_ClassWriter, MethodType.methodType(void.class, int.class));
        MethodHandle visit = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visit", MethodType.methodType(void.class, int.class, int.class, String.class, String.class, String.class, String[].class));
        MethodHandle visitEnd = TRUSTED_LOOKUP.findVirtual(CLASS_ClassWriter, "visitEnd", MethodType.methodType(void.class));

        Object classWriter = constructor.invoke(2 /*COMPUTE_FRAMES*/);
        visit.invoke(classWriter, this.opcode("V1_8"), access, name, signature, superName, interfaces);
        ASMClassBuilder builder = new ASMClassBuilder(classWriter, name);
        consumer.accept(builder);
        visitEnd.invoke(classWriter);

        return new ASMBuiltClass(classWriter, name);
    }

    @Override
    @SneakyThrows
    public BytecodeLabel label() {
        MethodHandle constructor = TRUSTED_LOOKUP.findConstructor(CLASS_Label, MethodType.methodType(void.class));
        return new BytecodeLabel(constructor.invoke());
    }

    @Override
    @SneakyThrows
    public BytecodeType type(String descriptor) {
        MethodHandle method = TRUSTED_LOOKUP.findStatic(CLASS_type, "getType", MethodType.methodType(CLASS_type, String.class));
        return new BytecodeType(method.invoke(descriptor));
    }

    @Override
    public int opcode(String name) {
        return opcodes.get(name.toUpperCase(Locale.ROOT));
    }

}
