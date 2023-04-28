package net.lenni0451.reflect;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void addEnumInstance() {
        DayOfWeek dayOfWeek = assertDoesNotThrow(() -> Enums.newInstance(DayOfWeek.class, "KYJSDAY", 8, new Class<?>[0], new Object[0]));
        assertNotNull(dayOfWeek);
        assertDoesNotThrow(() -> Enums.addEnumInstance(DayOfWeek.class, dayOfWeek));
        assertEquals(8, DayOfWeek.values().length);
        assertEquals(DayOfWeek.values()[7], dayOfWeek);
    }

    @Test
    void valueOfIgnoreCase() {
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "monday"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "MONDAY"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "Monday"));
        assertEquals(DayOfWeek.MONDAY, Enums.valueOfIgnoreCase(DayOfWeek.class, "mONdAY"));
    }

}
