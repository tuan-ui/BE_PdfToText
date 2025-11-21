package com.noffice.entity;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void dataAnnotation_Works() {
        Domain domain = new Domain();
        domain.setDomainCode("DOM001");
        domain.setDomainName("Phòng Hành chính");
        domain.setDomainDescription("Mô tả phòng ban");

        assertEquals("DOM001", domain.getDomainCode());
        assertEquals("Phòng Hành chính", domain.getDomainName());
    }

    @Test
    void constructors_Works() {
        Domain empty = new Domain();
        assertNotNull(empty);

        Domain full = new Domain("CODE123", "Tên Domain", "Mô tả dài");
        assertEquals("CODE123", full.getDomainCode());
        assertEquals("Tên Domain", full.getDomainName());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        Domain d1 = new Domain("ABC", "Tên", "Desc");
        Domain d2 = new Domain("ABC", "Tên", "Desc");

        // Dù id, createAt... khác nhau (do BaseEntity), nhưng equals vẫn true
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        Domain d1 = new Domain("CODE1", "Name", null);
        Domain d2 = new Domain("CODE2", "Name", null);

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        Domain domain = new Domain("TEST", "Test Domain", "OK");
        String str = domain.toString();

        assertTrue(str.contains("Domain"));
        assertTrue(str.contains("domainCode=TEST"));
        assertFalse(str.contains("BaseEntity"));
    }
}