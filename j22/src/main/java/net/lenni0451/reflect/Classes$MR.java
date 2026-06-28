package net.lenni0451.reflect;

import lombok.SneakyThrows;

import java.util.Set;

class Classes$MR {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.DROP_METHOD_INFO));

    @SneakyThrows
    public static Class<?> getCallerClass(final int depth) {
        return STACK_WALKER.walk(s -> s
                        .skip(depth + 2)
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass))
                .orElse(null);
    }

}
