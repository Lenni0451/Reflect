package net.lenni0451.reflect;

import lombok.SneakyThrows;

/**
 * This class contains methods which need to be replaced by other implementations for newer JDKs.
 */
class Classes$MR {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @SneakyThrows
    public static Class<?> getCallerClass(final int depth) {
        return STACK_WALKER.walk(s -> s
                .skip(depth + 3)
                .findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass)
                .orElse(null));
    }

}
