package net.lenni0451.reflect.localcapture;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;
import static net.lenni0451.reflect.utils.FieldInitializer.reqInit;

public class LocalCapturer {

    private static final Class<?> liveStackFrame = Classes.forName("java.lang.LiveStackFrame");
    private static final StackWalker stackWalker = reqInit(
            () -> {
                MethodHandle getStackWalker = TRUSTED_LOOKUP.findStatic(liveStackFrame, "getStackWalker", MethodType.methodType(StackWalker.class, Set.class));
                return (StackWalker) getStackWalker.invokeExact(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
            },
            () -> new MethodNotFoundException(liveStackFrame.getName(), "getStackWalker", StackWalker.class)
    );

    public static void forEach(final Consumer<LocalStackFrame> consumer) {
        walk(s -> {
            s.skip(1).forEach(consumer);
            return null;
        });
    }

    public static <T> T walk(final Function<Stream<LocalStackFrame>, T> function) {
        return stackWalker.walk(s -> function.apply(s.skip(1).map(LocalStackFrameImpl::new)));
    }

    public static LocalStackFrame[] getStackFrames() {
        return walk(s -> s.toArray(LocalStackFrame[]::new));
    }

}
