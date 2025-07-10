package net.lenni0451.reflect.bytecode.wrapper;

public class BytecodeType {

    private final Object handle;

    public BytecodeType(final Object handle) {
        this.handle = handle;
    }

    public Object getHandle() {
        return this.handle;
    }

}
