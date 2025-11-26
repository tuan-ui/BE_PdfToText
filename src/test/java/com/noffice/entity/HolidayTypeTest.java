package com.noffice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HolidayTypeTest {
    @Test
    void dataAnnotation_Works() {
        HolidayType holidayType = new HolidayType();
        holidayType.setHolidayTypeCode("DOM001");
        holidayType.setHolidayTypeName("Phòng Hành chính");
        holidayType.setDescription("Mô tả phòng ban");

        assertEquals("DOM001", holidayType.getHolidayTypeCode());
        assertEquals("Phòng Hành chính", holidayType.getHolidayTypeName());
    }

    @Test
    void constructors_Works() {
        HolidayType empty = new HolidayType();
        assertNotNull(empty);

        HolidayType full = new HolidayType("CODE123", "Tên HolidayType", "Mô tả dài");
        assertEquals("CODE123", full.getHolidayTypeCode());
        assertEquals("Tên HolidayType", full.getHolidayTypeName());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        HolidayType d1 = new HolidayType("ABC", "Tên", "Desc");
        HolidayType d2 = new HolidayType("ABC", "Tên", "Desc");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        HolidayType d1 = new HolidayType("CODE1", "Name", null);
        HolidayType d2 = new HolidayType("CODE2", "Name", null);

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        HolidayType holidayType = new HolidayType("TEST", "Test HolidayType", "OK");
        String str = holidayType.toString();

        assertTrue(str.contains("HolidayType"));
        assertTrue(str.contains("holidayTypeCode=TEST"));
        assertFalse(str.contains("BaseEntity"));
    }
}
