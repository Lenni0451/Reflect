package net.lenni0451.reflect.localcapture;

import net.lenni0451.commons.unchecked.FieldInitializer;
import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.exceptions.MethodNotFoundException;

import java.lang.invoke.MethodType;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.lenni0451.reflect.JVMConstants.CLASS_LiveStackFrame;
import static net.lenni0451.reflect.JVMConstants.METHOD_LiveStackFrame_getStackWalker;
import static net.lenni0451.reflect.JavaBypass.TRUSTED_LOOKUP;

public class LocalCapturer {

    private static final Class<?> liveStackFrame = Classes.forName(CLASS_LiveStackFrame);
    private static final StackWalker stackWalker = FieldInitializer
            .attempt(() -> TRUSTED_LOOKUP.findStatic(liveStackFrame, METHOD_LiveStackFrame_getStackWalker, MethodType.methodType(StackWalker.class, Set.class)))
            .map(m -> (StackWalker) m.invokeExact(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE)))
            .require(() -> new MethodNotFoundException(liveStackFrame.getName(), METHOD_LiveStackFrame_getStackWalker, StackWalker.class));

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
