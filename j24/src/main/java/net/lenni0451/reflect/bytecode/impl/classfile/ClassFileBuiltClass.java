package net.lenni0451.reflect.bytecode.impl.classfile;

import net.lenni0451.reflect.bytecode.wrapper.BuiltClass;

public class ClassFileBuiltClass implements BuiltClass {

    private final String name;
    private final byte[] bytes;

    public ClassFileBuiltClass(final String name, final byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public byte[] toBytes() {
        return this.bytes;
    }

}
