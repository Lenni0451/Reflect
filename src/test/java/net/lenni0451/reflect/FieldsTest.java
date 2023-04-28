package net.lenni0451.reflect;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class FieldsTest {

    private static FieldsClass fc;

    @BeforeAll
    static void setUp() {
        fc = new FieldsClass();
        fc.bool = true;
        fc.b = 1;
        fc.s = 2;
        fc.c = 3;
        fc.i = 4;
        fc.l = 5;
        fc.f = 6;
        fc.d = 7;
        fc.str = "8";
    }

    @Test
    void offset() {
        Field f = assertDoesNotThrow(() -> FieldsClass.class.getDeclaredField("bool"));
        assertDoesNotThrow(() -> Fields.offset(f));
    }

    @Test
    void getDeclaredFields() {
        Field[] fields = assertDoesNotThrow(() -> Fields.getDeclaredFields(FieldsClass.class));
        assertNotNull(fields);
        assertEquals(9, fields.length);
    }

    @Test
    void get() {
        for (Field field : FieldsClass.class.getDeclaredFields()) {
            Object value = assertDoesNotThrow(() -> Fields.get(fc, field));
            assertNotNull(value);
            if (field.getName().equalsIgnoreCase("bool")) assertEquals(true, value);
            else if (field.getName().equalsIgnoreCase("b")) assertEquals((byte) 1, value);
            else if (field.getName().equalsIgnoreCase("s")) assertEquals((short) 2, value);
            else if (field.getName().equalsIgnoreCase("c")) assertEquals((char) 3, value);
            else if (field.getName().equalsIgnoreCase("i")) assertEquals(4, value);
            else if (field.getName().equalsIgnoreCase("l")) assertEquals(5L, value);
            else if (field.getName().equalsIgnoreCase("f")) assertEquals(6F, value);
            else if (field.getName().equalsIgnoreCase("d")) assertEquals(7D, value);
            else if (field.getName().equalsIgnoreCase("str")) assertEquals("8", value);
            else fail("Unknown field: " + field.getName());
        }
    }

    @Test
    void set() {
        FieldsClass fc = new FieldsClass();
        for (Field field : FieldsClass.class.getDeclaredFields()) Fields.set(fc, field, assertDoesNotThrow(() -> field.get(FieldsTest.fc)));
        assertEquals(FieldsTest.fc, fc);
    }

    @Test
    void copy() {
        FieldsClass fc = new FieldsClass();
        for (Field field : FieldsClass.class.getDeclaredFields()) Fields.copy(FieldsTest.fc, fc, field);
        assertEquals(FieldsTest.fc, fc);
    }


    private static class FieldsClass {
        public boolean bool;
        public byte b;
        public short s;
        public char c;
        public int i;
        public long l;
        public float f;
        public double d;
        public String str;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldsClass that = (FieldsClass) o;
            return bool == that.bool && b == that.b && s == that.s && c == that.c && i == that.i && l == that.l && Float.compare(that.f, f) == 0 && Double.compare(that.d, d) == 0 && Objects.equals(str, that.str);
        }
    }

}
