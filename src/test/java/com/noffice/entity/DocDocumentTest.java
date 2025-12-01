package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocDocumentTest {
    private final UUID tempId = UUID.randomUUID();
    @Test
    void dataAnnotation_Works() {
        DocDocument docDocument = new DocDocument();
        docDocument.setDocTemplateId(tempId);
        docDocument.setDocTypeId(tempId);
        docDocument.setDocumentTitle("docTitle");
        docDocument.setDeptName("deptName");
        docDocument.setPurpose("purpose");
        docDocument.setFormData("formData");
        docDocument.setDocTypeName("docTypeName");

        assertEquals(tempId, docDocument.getDocTemplateId());
        assertEquals(tempId, docDocument.getDocTypeId());
        assertEquals("docTitle", docDocument.getDocumentTitle());
        assertEquals("deptName", docDocument.getDeptName());
        assertEquals("purpose", docDocument.getPurpose());
        assertEquals("formData", docDocument.getFormData());
        assertEquals("docTypeName", docDocument.getDocTypeName());
    }

    @Test
    void constructors_Works() {
        DocDocument empty = new DocDocument();
        assertNotNull(empty);

        DocDocument full = new DocDocument(tempId,tempId, "docTitle", "deptName", "purpose", "formData", "docTypeName");
        assertEquals(tempId, full.getDocTemplateId());
        assertEquals(tempId, full.getDocTypeId());
        assertEquals("docTitle", full.getDocumentTitle());
        assertEquals("deptName", full.getDeptName());
        assertEquals("purpose", full.getPurpose());
        assertEquals("formData", full.getFormData());
        assertEquals("docTypeName", full.getDocTypeName());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocDocument d1 = new DocDocument(tempId,tempId, "docTitle", "deptName", "purpose", "formData", "docTypeName");
        DocDocument d2 = new DocDocument(tempId,tempId, "docTitle", "deptName", "purpose", "formData", "docTypeName");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocDocument d1 = new DocDocument(tempId,tempId, "docTitle 1", "deptName", "purpose", "formData", "docTypeName");
        DocDocument d2 = new DocDocument(tempId,tempId, "docTitle 2", "deptName", "purpose", "formData", "docTypeName");

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        DocDocument docDocument = new DocDocument(tempId,tempId, "docTitle 1", "deptName", "purpose", "formData", "docTypeName");
        String str = docDocument.toString();

        assertTrue(str.contains("DocDocument"));
        assertTrue(str.contains("documentTitle=docTitle 1"));
        assertFalse(str.contains("BaseEntity"));
    }
}
