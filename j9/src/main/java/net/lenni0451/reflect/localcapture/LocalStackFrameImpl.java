package net.lenni0451.reflect.localcapture;

import lombok.Getter;
import lombok.SneakyThrows;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.exceptions.FieldNotFoundException;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

class LocalStackFrameImpl implements LocalStackFrame {

    private static final Class<?> liveStackFrameInfo = Classes.forName("java.lang.LiveStackFrameInfo");
    private static final Class<?> primitiveSlot32 = Classes.forName("java.lang.LiveStackFrameInfo$PrimitiveSlot32");
    private static final Class<?> primitiveSlot64 = Classes.forName("java.lang.LiveStackFrameInfo$PrimitiveSlot64");

    private static final MethodHandle liveStackFrameInfo_getMonitors = reqInit(
            () -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, "getMonitors", MethodType.methodType(Object[].class)),
            handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)),
            () -> new MethodNotFoundException(liveStackFrameInfo.getName(), "getMonitors", Object[].class)
    );
    private static final MethodHandle liveStackFrameInfo_getLocals = reqInit(
            () -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, "getLocals", MethodType.methodType(Object[].class)),
            handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)),
            () -> new MethodNotFoundException(liveStackFrameInfo.getName(), "getLocals", Object[].class)
    );
    private static final MethodHandle liveStackFrameInfo_getStack = reqInit(
            () -> TRUSTED_LOOKUP.findVirtual(liveStackFrameInfo, "getStack", MethodType.methodType(Object[].class)),
            handle -> handle.asType(MethodType.methodType(Object[].class, StackWalker.StackFrame.class)),
            () -> new MethodNotFoundException(liveStackFrameInfo.getName(), "getStack", Object[].class)
    );
    private static final MethodHandle liveStackFrameInfo_mode = reqInit(
            () -> TRUSTED_LOOKUP.findGetter(liveStackFrameInfo, "mode", int.class),
            handle -> handle.asType(MethodType.methodType(int.class, StackWalker.StackFrame.class)),
            () -> new FieldNotFoundException(liveStackFrameInfo.getName(), "mode")
    );
    private static final MethodHandle primitiveSlot32_value = reqInit(
            () -> TRUSTED_LOOKUP.findGetter(primitiveSlot32, "value", int.class),
            handle -> handle.asType(MethodType.methodType(int.class, Object.class)),
            () -> new FieldNotFoundException(primitiveSlot32.getName(), "value")
    );
    private static final MethodHandle primitiveSlot64_value = reqInit(
            () -> TRUSTED_LOOKUP.findGetter(primitiveSlot64, "value", long.class),
            handle -> handle.asType(MethodType.methodType(long.class, Object.class)),
            () -> new FieldNotFoundException(primitiveSlot64.getName(), "value")
    );

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
