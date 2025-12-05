package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttachsTest {
    private final UUID tempId = UUID.randomUUID();
    private final LocalDateTime date = LocalDateTime.now();
    @Test
    void dataAnnotation() {
        Attachs attachs = new Attachs();
        attachs.setObjectId(tempId);
        attachs.setObjectType(1);
        attachs.setAttachName("Tệp đính kèm");
        attachs.setAttachPath("attachPath");
        attachs.setCreatorId(tempId);
        attachs.setDateCreate(date);
        attachs.setAttachType(1);
        attachs.setSavePath("savePath");

        assertEquals(tempId, attachs.getObjectId());
        assertEquals(1, attachs.getObjectType());
        assertEquals("Tệp đính kèm", attachs.getAttachName());
        assertEquals("attachPath", attachs.getAttachPath());
        assertEquals(tempId, attachs.getCreatorId());
        assertEquals(date, attachs.getDateCreate());
        assertEquals(1, attachs.getAttachType());
        assertEquals("savePath", attachs.getSavePath());
    }

    @Test
    void constructors() {
        Attachs empty = new Attachs();
        assertNotNull(empty);

        Attachs full = new Attachs(tempId, 1, "Tệp đính kèm","attachPath", tempId, date, 1, "savePath");
        assertEquals(tempId, full.getObjectId());
        assertEquals(1, full.getObjectType());
        assertEquals("Tệp đính kèm", full.getAttachName());
        assertEquals("attachPath", full.getAttachPath());
        assertEquals(tempId, full.getCreatorId());
        assertEquals(date, full.getDateCreate());
        assertEquals(1, full.getAttachType());
        assertEquals("savePath", full.getSavePath());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        Attachs full = Attachs.builder()
                .objectId(tempId)
                .objectType(1)
                .attachName("Tệp đính kèm")
                .attachPath("attachPath")
                .creatorId(tempId)
                .dateCreate(date)
                .attachType(1)
                .savePath("savePath")
                .build();
        assertEquals(tempId, full.getObjectId());
        assertEquals(1, full.getObjectType());
        assertEquals("Tệp đính kèm", full.getAttachName());
        assertEquals("attachPath", full.getAttachPath());
        assertEquals(tempId, full.getCreatorId());
        assertEquals(date, full.getDateCreate());
        assertEquals(1, full.getAttachType());
        assertEquals("savePath", full.getSavePath());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        Attachs d1 = new Attachs(tempId, 1, "Tệp đính kèm 1","attachPath", tempId, date, 1, "savePath");
        Attachs d2 = new Attachs(tempId, 1, "Tệp đính kèm 2","attachPath", tempId, date, 1, "savePath");

        assertNotEquals(d1, d2);
    }

}
