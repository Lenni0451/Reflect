package net.lenni0451.reflect.bytecode.wrapper;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class BytecodeLabel {

    private final Object handle;

    public BytecodeLabel(final Object handle) {
        this.handle = handle;
    }

    public Object getHandle() {
        return this.handle;
    }

}
