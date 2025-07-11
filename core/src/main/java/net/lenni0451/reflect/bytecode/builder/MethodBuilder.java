package net.lenni0451.reflect.bytecode.builder;

import net.lenni0451.reflect.bytecode.wrapper.BytecodeLabel;
import org.jetbrains.annotations.ApiStatus;

import static net.lenni0451.reflect.bytecode.BytecodeUtils.*;

@ApiStatus.Experimental
public interface MethodBuilder {

    MethodBuilder return_();

    MethodBuilder ireturn();

    MethodBuilder lreturn();

    MethodBuilder freturn();

    MethodBuilder dreturn();

    MethodBuilder areturn();

    default MethodBuilder return_(final Class<?> type) {
        if (void.class.equals(type)) return this.return_();
        if (boolean.class.equals(type) || byte.class.equals(type) || char.class.equals(type) || short.class.equals(type) || int.class.equals(type)) return this.ireturn();
        if (long.class.equals(type)) return this.lreturn();
        if (float.class.equals(type)) return this.freturn();
        if (double.class.equals(type)) return this.dreturn();
        return this.areturn();
    }

    MethodBuilder dup();

    MethodBuilder dupX1();

    MethodBuilder pop();

    MethodBuilder aaload();

    MethodBuilder aastore();

    MethodBuilder aconstNull();

    MethodBuilder athrow();

    MethodBuilder i2l();

    MethodBuilder iconstM1();

    MethodBuilder iconst0();

    MethodBuilder iconst1();

    MethodBuilder iconst2();

    MethodBuilder iconst3();

    MethodBuilder iconst4();

    MethodBuilder iconst5();

    MethodBuilder lconst0();

    MethodBuilder lconst1();

    MethodBuilder fconst0();

    MethodBuilder fconst1();

    MethodBuilder fconst2();

    MethodBuilder dconst0();

    MethodBuilder dconst1();

    MethodBuilder sipush(final int value);

    MethodBuilder bipush(final int value);

    default MethodBuilder intPush(final int i) {
        if (i == -1) return this.iconstM1();
        if (i == 0) return this.iconst0();
        if (i == 1) return this.iconst1();
        if (i == 2) return this.iconst2();
        if (i == 3) return this.iconst3();
        if (i == 4) return this.iconst4();
        if (i == 5) return this.iconst5();
        if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) return this.bipush(i);
        if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) return this.sipush(i);
        return this.ldc(i);
    }

    MethodBuilder iload(final int varIndex);

    MethodBuilder lload(final int varIndex);

    MethodBuilder fload(final int varIndex);

    MethodBuilder dload(final int varIndex);

    MethodBuilder aload(final int varIndex);

    default MethodBuilder load(final Class<?> type, final int varIndex) {
        if (boolean.class.equals(type) || byte.class.equals(type) || char.class.equals(type) || short.class.equals(type) || int.class.equals(type)) return this.iload(varIndex);
        if (long.class.equals(type)) return this.lload(varIndex);
        if (float.class.equals(type)) return this.fload(varIndex);
        if (double.class.equals(type)) return this.dload(varIndex);
        return this.aload(varIndex);
    }

    MethodBuilder istore(final int varIndex);

    MethodBuilder lstore(final int varIndex);

    MethodBuilder fstore(final int varIndex);

    MethodBuilder dstore(final int varIndex);

    MethodBuilder astore(final int varIndex);

    default MethodBuilder store(final Class<?> type, final int varIndex) {
        if (boolean.class.equals(type) || byte.class.equals(type) || char.class.equals(type) || short.class.equals(type) || int.class.equals(type)) return this.istore(varIndex);
        if (long.class.equals(type)) return this.lstore(varIndex);
        if (float.class.equals(type)) return this.fstore(varIndex);
        if (double.class.equals(type)) return this.dstore(varIndex);
        return this.astore(varIndex);
    }

    MethodBuilder new_(final String type);

    MethodBuilder checkcast(final String type);

    MethodBuilder anewarray(final String type);

    default MethodBuilder box(final Class<?> primitive) {
        Class<?> boxed = boxed(primitive);
        if (boxed != primitive) {
            this.invokestatic(slash(boxed), "valueOf", mdesc(boxed, primitive), false);
        }
        return this;
    }

    default MethodBuilder unbox(final Class<?> primitive) {
        Class<?> boxed = boxed(primitive);
        if (boxed != primitive) {
            this.invokevirtual(slash(boxed), primitive.getSimpleName() + "Value", mdesc(primitive));
        }
        return this;
    }

    MethodBuilder putfield(final String owner, final String name, final String descriptor);

    MethodBuilder putstatic(final String owner, final String name, final String descriptor);

    MethodBuilder getfield(final String owner, final String name, final String descriptor);

    MethodBuilder getstatic(final String owner, final String name, final String descriptor);

    MethodBuilder invokespecial(final String owner, final String name, final String descriptor, final boolean isInterface);

    MethodBuilder invokeinterface(final String owner, final String name, final String descriptor);

    MethodBuilder invokevirtual(final String owner, final String name, final String descriptor);

    MethodBuilder invokestatic(final String owner, final String name, final String descriptor, final boolean isInterface);

    MethodBuilder ifne(final BytecodeLabel label);

    MethodBuilder ifnonnull(final BytecodeLabel label);

    MethodBuilder goto_(final BytecodeLabel label);

    BytecodeLabel newLabel();

    MethodBuilder label(final BytecodeLabel label);

    MethodBuilder ldc(final Object value);

    default MethodBuilder typeLdc(final BytecodeBuilder builder, final Class<?> clazz) {
        Class<?> boxed = boxed(clazz);
        if (boxed == clazz) {
            this.ldc(builder.type(desc(clazz)));
        } else {
            this.getstatic(slash(boxed), "TYPE", desc(Class.class));
        }
        return this;
    }

    MethodBuilder tryCatch(final BytecodeLabel start, final BytecodeLabel end, final BytecodeLabel handler, final String type);

    MethodBuilder maxs(final int maxStack, final int maxLocals);

}
