package net.lenni0451.reflect.stream.utils;

import java.util.function.Supplier;

public class Lazy<T> {

    private final Supplier<T> supplier;
    private T value;
    private boolean isSet;

    public Lazy(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!this.isSet) {
            this.value = this.supplier.get();
            this.isSet = true;
        }

        return value;
    }

}
