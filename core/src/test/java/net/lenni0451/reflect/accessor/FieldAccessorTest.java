package net.lenni0451.reflect.accessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldAccessorTest {

    private FieldClass fc;
    private Field field;

    @BeforeEach
    void setUp() {
        this.fc = new FieldClass();
        this.field = assertDoesNotThrow(() -> FieldClass.class.getDeclaredField("field"));
    }

    @Test
    void makeSetter() {
        Consumer<String> setter = assertDoesNotThrow(() -> FieldAccessor.makeSetter(Consumer.class, this.fc, this.field));
        setter.accept("World");
        assertEquals("World", this.fc.getField());
    }

    @Test
    void makeDynamicSetter() {
        BiConsumer<FieldClass, String> dynamicSetter = assertDoesNotThrow(() -> FieldAccessor.makeDynamicSetter(BiConsumer.class, this.field));
        dynamicSetter.accept(this.fc, "World");
        assertEquals("World", this.fc.getField());
    }

    @Test
    void makeGetter() {
        Supplier<String> getter = assertDoesNotThrow(() -> FieldAccessor.makeGetter(Supplier.class, this.fc, this.field));
        assertEquals("Hello", getter.get());
    }

    @Test
    void makeDynamicGetter() {
        Function<FieldClass, String> dynamicGetter = assertDoesNotThrow(() -> FieldAccessor.makeDynamicGetter(Function.class, this.field));
        assertEquals("Hello", dynamicGetter.apply(this.fc));
    }


    @SuppressWarnings("FieldMayBeFinal")
    private static class FieldClass {
        private String field = "Hello";

        public String getField() {
            return this.field;
        }
    }

}
