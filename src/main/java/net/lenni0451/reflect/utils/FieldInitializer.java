package net.lenni0451.reflect.utils;

import lombok.SneakyThrows;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Helper class to initialize fields with a value or throw an exception if the value is null.
 */
public class FieldInitializer {

    public static <T> T init(final T value, final Consumer<T> initializer) {
        initializer.accept(value);
        return value;
    }

    @SneakyThrows
    public static <T> T init(final ThrowingSupplier<T> supplier) {
        return supplier.get();
    }

    @SneakyThrows
    public static <T, R> R init(final ThrowingSupplier<T> supplier, final ThrowingFunction<T, R> processor) {
        return processor.apply(supplier.get());
    }

    @SneakyThrows
    public static <T> T reqInit(final ThrowingSupplier<T> supplier, final Supplier<Throwable> exceptionSupplier) {
        T value = supplier.get();
        if (value == null) throw exceptionSupplier.get();
        return value;
    }

    @SneakyThrows
    public static <T, R> R reqInit(final ThrowingSupplier<T> supplier, final ThrowingFunction<T, R> processor, final Supplier<Throwable> exceptionSupplier) {
        T value = supplier.get();
        if (value == null) throw exceptionSupplier.get();
        return processor.apply(value);
    }

    @SneakyThrows
    public static <T> T condInit(final boolean init, final ThrowingSupplier<T> supplier) {
        if (!init) return null;
        return supplier.get();
    }

    @SneakyThrows
    public static <T, R> R condInit(final boolean init, final ThrowingSupplier<T> supplier, final ThrowingFunction<T, R> processor) {
        if (!init) return null;
        return processor.apply(supplier.get());
    }

    public static <T> T optInit(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            return null;
        }
    }

    public static <T, R> R optInit(final ThrowingSupplier<T> supplier, final ThrowingFunction<T, R> processor) {
        try {
            return processor.apply(supplier.get());
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

    @SneakyThrows
    public static <T, R> R reqOptInit(final boolean init, final ThrowingSupplier<T> supplier, final ThrowingFunction<T, R> processor, final Supplier<Throwable> exceptionSupplier) {
        if (!init) return null;
        T value = supplier.get();
        if (value == null) throw exceptionSupplier.get();
        return processor.apply(value);
    }


    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingFunction<A, R> {
        R apply(final A a) throws Throwable;
    }

}
