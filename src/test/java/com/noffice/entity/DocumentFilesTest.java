package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DocumentFilesTest {
    private final UUID tempId = UUID.randomUUID();
    private final LocalDateTime date = LocalDateTime.now();
    @Test
    void dataAnnotation() {
        DocumentFiles documentFiles = new DocumentFiles();
        documentFiles.setAttachName("name");
        documentFiles.setAttachPath("path");
        documentFiles.setOriginalFileId(tempId);
        documentFiles.setIsTemp(null);
        documentFiles.setLockValue("lock");
        documentFiles.setLockExpiresAt(date);
        documentFiles.setLockUserId(tempId);

        assertEquals("name", documentFiles.getAttachName());
        assertEquals("path", documentFiles.getAttachPath());
        assertEquals(tempId, documentFiles.getOriginalFileId());
        assertNull(documentFiles.getIsTemp());
        assertEquals("lock", documentFiles.getLockValue());
        assertEquals(date, documentFiles.getLockExpiresAt());
        assertEquals(tempId, documentFiles.getLockUserId());
    }

    @Test
    void constructors() {
        DocumentFiles empty = new DocumentFiles();
        assertNotNull(empty);

        DocumentFiles full = new DocumentFiles("name", "path", tempId,true,"lock", date, tempId);
        assertEquals("name", full.getAttachName());
        assertEquals("path", full.getAttachPath());
        assertEquals(tempId, full.getOriginalFileId());
        assertEquals(true,full.getIsTemp());
        assertEquals("lock", full.getLockValue());
        assertEquals(date, full.getLockExpiresAt());
        assertEquals(tempId, full.getLockUserId());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocumentFiles d1 = new DocumentFiles("name", "path", tempId,true,"lock", date, tempId);
        DocumentFiles d2 = new DocumentFiles("name", "path", tempId,true,"lock", date, tempId);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocumentFiles d1 = new DocumentFiles("name", "path", tempId,true,"lock", date, tempId);
        DocumentFiles d2 = new DocumentFiles("name", "path", tempId,false,"lock", date, tempId);

        assertNotEquals(d1, d2);
    }
    @Test
    void testDefaultIsTempValue() {
        DocumentFiles documentFiles = new DocumentFiles();
        // isTemp mặc định phải là false
        assertThat(documentFiles.getIsTemp()).isFalse();
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        DocumentFiles df1 = new DocumentFiles();
        df1.setAttachName("file1.pdf");
        df1.setAttachPath("/tmp/file1.pdf");
        df1.setOriginalFileId(id);

        DocumentFiles df2 = new DocumentFiles();
        df2.setAttachName("file1.pdf");
        df2.setAttachPath("/tmp/file1.pdf");
        df2.setOriginalFileId(id);

        assertThat(df1).isEqualTo(df2);
        assertThat(df1.hashCode()).hasSameHashCodeAs(df2.hashCode());
    }

    @Test
    void testToStringContainsFieldNames() {
        DocumentFiles df = new DocumentFiles();
        df.setAttachName("file.pdf");
        df.setAttachPath("/tmp/file.pdf");

        String str = df.toString();
        assertThat(str).contains("attachName", "file.pdf");
        assertThat(str).contains("attachPath", "/tmp/file.pdf");
    }
}
