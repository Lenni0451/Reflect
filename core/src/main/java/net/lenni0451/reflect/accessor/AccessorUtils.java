package net.lenni0451.reflect.accessor;

import net.lenni0451.reflect.bytecode.builder.BytecodeBuilder;
import net.lenni0451.reflect.bytecode.builder.ClassBuilder;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

/**
 * Utils for creating accessors for fields and methods.
 */
@ApiStatus.Internal
class AccessorUtils {

    private static final String VALID_MEMBER_NAME = "[^a-zA-Z0-9_$]";

    public static String makeAccessorName(final String type, final Class<?> owner, final String memberName) {
        StringBuilder name = new StringBuilder();
        Package pkg = owner.getPackage();
        if (pkg != null) name.append(slash(pkg.getName())).append('/');
        name.append(owner.getSimpleName().replaceAll(VALID_MEMBER_NAME, "_")).append('$');
        name.append(memberName.replaceAll(VALID_MEMBER_NAME, "_")).append('$');
        name.append(type);
        return name.toString();
    }

    public static void addConstructor(final BytecodeBuilder builder, final ClassBuilder cb, @Nullable final Supplier<Class<?>> instanceType, final boolean isStatic) {
        if (isStatic || instanceType == null) {
            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class), null, null, mb -> mb
                    .aload(0)
                    .invokespecial(slash(Object.class), "<init>", mdesc(void.class), false)
                    .return_()
                    .maxs(1, 1)
            );
        } else {
            cb.field(builder.opcode("ACC_PRIVATE", "ACC_FINAL"), "instance", desc(instanceType.get()), null, null, fb -> {});

            cb.method(builder.opcode("ACC_PUBLIC"), "<init>", mdesc(void.class, instanceType.get()), null, null, mb -> mb
                    .aload(0)
                    .invokespecial(slash(Object.class), "<init>", mdesc(void.class), false)
                    .aload(0)
                    .aload(1)
                    .putfield(cb.getName(), "instance", desc(instanceType.get()))
                    .return_()
                    .maxs(2, 2)
            );
        }
    }

}
