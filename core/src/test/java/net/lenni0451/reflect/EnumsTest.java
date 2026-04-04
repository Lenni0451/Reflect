package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void addEnumInstance() {
        assertEquals(7, DayOfWeek.class.getEnumConstants().length);
        DayOfWeek dayOfWeek = assertDoesNotThrow(() -> Enums.newInstance(DayOfWeek.class, "KYJSDAY", 8, new Class<?>[0], new Object[0]));
        assertNotNull(dayOfWeek);
        assertDoesNotThrow(() -> Enums.addEnumInstance(DayOfWeek.class, dayOfWeek));
        assertEquals(8, DayOfWeek.values().length);
        assertEquals(8, DayOfWeek.class.getEnumConstants().length);
        assertEquals(DayOfWeek.values()[7], dayOfWeek);
    }

    @Test
    void createVarargEnumInstance() {
        {
            VarargTestEnum instance = Enums.newInstance(VarargTestEnum.class, "THREE", 2, new Class[]{String.class, String[].class}, new Object[]{"three", "test5", "test6"});
            assertEquals("THREE", instance.name());
            assertEquals(2, instance.ordinal());
        }
        {
            VarargTestEnum instance = Enums.newInstance(VarargTestEnum.class, "THREE", 2, new Class[]{String.class, String[].class}, new Object[]{"three", new String[]{"test5", "test6"}});
            assertEquals("THREE", instance.name());
            assertEquals(2, instance.ordinal());
        }
    }

    @Test
    void valueOfIgnoreCase() {
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "monday"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "MONDAY"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "Monday"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "mONdAY"));
    }


    public enum VarargTestEnum {
        ONE("one", "test1", "test2"),
        TWO("two", "test3", "test4");

        VarargTestEnum(String arg, String... varargs) {
        }
    }

}
