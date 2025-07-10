package net.lenni0451.reflect.localcapture;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This class is used to capture the local stack frames of the current thread.<br>
 * The stack frames contain monitors, locals and stack slots of the current thread.
 */
public class LocalCapturer {

    /**
     * Iterate over all stack frames of the current thread.
     *
     * @param consumer The consumer to call for each stack frame
     */
    public static void forEach(final Consumer<LocalStackFrame> consumer) {
        throw new UnsupportedOperationException("Not supported in Java 8");
    }

    /**
     * Walk over the stack frames of the current thread and return the result of the function.
     *
     * @param function The function to call for each stack frame
     * @param <T>      The type of the result
     * @return The result of the function
     * @see StackWalker#walk(Function)
     */
    public static <T> T walk(final Function<Stream<LocalStackFrame>, T> function) {
        throw new UnsupportedOperationException("Not supported in Java 8");
    }

    /**
     * Get all stack frames of the current thread.
     *
     * @return An array of all stack frames of the current thread
     */
    public static LocalStackFrame[] getStackFrames() {
        throw new UnsupportedOperationException("Not supported in Java 8");
    }

}
