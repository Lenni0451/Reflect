package net.lenni0451.reflect.utils;

import lombok.SneakyThrows;

import java.util.function.Supplier;

public class FieldInitializer {

    @SneakyThrows
    public static <T> T init(final ThrowingSupplier<T> supplier) {
        return supplier.get();
    }

    @SneakyThrows
    public static <T> T reqInit(final ThrowingSupplier<T> supplier, final Supplier<Throwable> exceptionSupplier) {
        T value = supplier.get();
        if (value == null) throw exceptionSupplier.get();
        return value;
    }

    @SneakyThrows
    public static <T> T condInit(final boolean init, final ThrowingSupplier<T> supplier) {
        if (!init) return null;
        return supplier.get();
    }

    public static <T> T optInit(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            return null;
        }
    }

    @SneakyThrows
    public static <T> T reqOptInit(final boolean init, final ThrowingSupplier<T> supplier, final Supplier<Throwable> exceptionSupplier) {
        if (!init) return null;
        T value = supplier.get();
        if (value == null) throw exceptionSupplier.get();
        return value;
    }


    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

}
