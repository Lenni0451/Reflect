package net.lenni0451.reflect.bytecode.wrapper;

import net.lenni0451.reflect.ClassLoaders;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface BuiltClass {

    String getName();

    byte[] toBytes();

    default Class<?> defineAnonymous(final Class<?> parent) {
        return ClassLoaders.defineAnonymousClass(parent, this.toBytes());
    }

    default Class<?> defineMetafactory(final Class<?> parent) {
        return ClassLoaders.defineAnonymousClass(parent, this.toBytes(), "NESTMATE", "STRONG");
    }

}
