package com.noffice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocTypeTest {
    @Test
    void dataAnnotation_Works() {
        DocType docType = new DocType();
        docType.setDocTypeCode("DOM001");
        docType.setDocTypeName("Phòng Hành chính");
        docType.setDocTypeDescription("Mô tả phòng ban");

        assertEquals("DOM001", docType.getDocTypeCode());
        assertEquals("Phòng Hành chính", docType.getDocTypeName());
    }

    @Test
    void constructors_Works() {
        DocType empty = new DocType();
        assertNotNull(empty);

        DocType full = new DocType("CODE123", "Tên DocType", "Mô tả dài");
        assertEquals("CODE123", full.getDocTypeCode());
        assertEquals("Tên DocType", full.getDocTypeName());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocType d1 = new DocType("ABC", "Tên", "Desc");
        DocType d2 = new DocType("ABC", "Tên", "Desc");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocType d1 = new DocType("CODE1", "Name", null);
        DocType d2 = new DocType("CODE2", "Name", null);

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        DocType docType = new DocType("TEST", "Test DocType", "OK");
        String str = docType.toString();

        assertTrue(str.contains("DocType"));
        assertTrue(str.contains("docTypeCode=TEST"));
        assertFalse(str.contains("BaseEntity"));
    }
}
