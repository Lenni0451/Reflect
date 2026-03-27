package net.lenni0451.reflect.localcapture;

import lombok.Getter;
import lombok.SneakyThrows;
import net.lenni0451.commons.unchecked.FieldInitializer;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static net.lenni0451.reflect.JVMConstants.*;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

class LocalStackFrameImpl implements LocalStackFrame {

    private static final Class<?> liveStackFrameInfo = Classes.forName(CLASS_LiveStackFrameInfo);
    private static final Class<?> primitiveSlot32 = Classes.forName(CLASS_LiveStackFrameInfo_PrimitiveSlot32);
    private static final Class<?> primitiveSlot64 = Classes.forName(CLASS_LiveStackFrameInfo_PrimitiveSlot64);

    private static final MethodHandle liveStackFrameInfo_getMonitors = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, METHOD_LiveStackFrameInfo_getMonitors, MethodType.methodType(Object[].class)))
            .map(handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)))
            .require(() -> new MethodNotFoundException(liveStackFrameInfo.getName(), METHOD_LiveStackFrameInfo_getMonitors, Object[].class));
    private static final MethodHandle liveStackFrameInfo_getLocals = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, METHOD_LiveStackFrameInfo_getLocals, MethodType.methodType(Object[].class)))
            .map(handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)))
            .require(() -> new MethodNotFoundException(liveStackFrameInfo.getName(), METHOD_LiveStackFrameInfo_getLocals, Object[].class));
    private static final MethodHandle liveStackFrameInfo_getStack = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, METHOD_LiveStackFrameInfo_getStack, MethodType.methodType(Object[].class)))
            .map(handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)))
            .require(() -> new MethodNotFoundException(liveStackFrameInfo.getName(), METHOD_LiveStackFrameInfo_getStack, Object[].class));
    private static final MethodHandle liveStackFrameInfo_mode = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findGetter(liveStackFrameInfo, METHOD_LiveStackFrameInfo_mode, int.class))
            .map(handle -> handle.asType(MethodType.methodType(int.class, StackWalker.StackFrame.class)))
            .require(() -> new FieldNotFoundException(liveStackFrameInfo.getName(), METHOD_LiveStackFrameInfo_mode));
    private static final MethodHandle primitiveSlot32_value = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findGetter(primitiveSlot32, METHOD_LiveStackFrameInfo_PrimitiveSlot32_value, int.class))
            .map(handle -> handle.asType(MethodType.methodType(int.class, Object.class)))
            .require(() -> new FieldNotFoundException(primitiveSlot32.getName(), METHOD_LiveStackFrameInfo_PrimitiveSlot32_value));
    private static final MethodHandle primitiveSlot64_value = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findGetter(primitiveSlot64, METHOD_LiveStackFrameInfo_PrimitiveSlot64_value, long.class))
            .map(handle -> handle.asType(MethodType.methodType(long.class, Object.class)))
            .require(() -> new FieldNotFoundException(primitiveSlot64.getName(), METHOD_LiveStackFrameInfo_PrimitiveSlot64_value));

    private static final int MODE_INTERPRETED = 0x01;
    private static final int MODE_COMPILED = 0x02;

    private final StackWalker.StackFrame parent;
    @Getter(lazy = true)
    private final Object[] monitors = this.convertObjectArray(liveStackFrameInfo_getMonitors);
    @Getter(lazy = true)
    private final Object[] locals = this.convertObjectArray(liveStackFrameInfo_getLocals);
    @Getter(lazy = true)
    private final Object[] stack = this.convertObjectArray(liveStackFrameInfo_getStack);

    public LocalStackFrameImpl(final StackWalker.StackFrame parent) {
        if (!liveStackFrameInfo.isInstance(parent)) throw new IllegalArgumentException("The parent stack frame is not an instance of LiveStackFrameInfo");
        this.parent = parent;
    }

    @Override
    public String getClassName() {
        return this.parent.getClassName();
    }

    @Override
    public String getMethodName() {
        return this.parent.getMethodName();
    }

    @Override
    public Class<?> getDeclaringClass() {
        return this.parent.getDeclaringClass();
    }

    @Override
    public int getByteCodeIndex() {
        return this.parent.getByteCodeIndex();
    }

    @Override
    public String getFileName() {
        return this.parent.getFileName();
    }

    @Override
    public int getLineNumber() {
        return this.parent.getLineNumber();
    }

    @Override
    public boolean isNativeMethod() {
        return this.parent.isNativeMethod();
    }

    @Override
    public StackTraceElement toStackTraceElement() {
        return this.parent.toStackTraceElement();
    }

    @Override
    @SneakyThrows
    @MagicConstant(intValues = {MODE_INTERPRETED, MODE_COMPILED})
    public int getMode() {
        return (int) liveStackFrameInfo_mode.invokeExact(this.parent);
    }

    @SneakyThrows
    private Object[] convertObjectArray(final MethodHandle getter) {
        Object[] array = (Object[]) getter.invokeExact(this.parent);
        Object[] converted = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            Object item = array[i];
            if (primitiveSlot32.isInstance(item)) {
                converted[i] = new PrimitiveValue(4, (int) primitiveSlot32_value.invokeExact(item));
            } else if (primitiveSlot64.isInstance(item)) {
                converted[i] = new PrimitiveValue(8, (long) primitiveSlot64_value.invokeExact(item));
            } else {
                converted[i] = item;
            }
        }
        return converted;
    }

}
