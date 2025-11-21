package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttachDTOTest {

    private static final UUID UUID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID UUID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void noArgsConstructor_Works() {
        AttachDTO dto = new AttachDTO();
        assertNotNull(dto);
    }

    @Test
    void allArgsConstructor_SetsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        AttachDTO dto = new AttachDTO(
                UUID_1, UUID_2, 99, "file.pdf", "/path/to/file",
                UUID_1, now, 1, "/save/path", 10L,
                true, false, now, now, now, UUID_1, UUID_1, UUID_1, UUID_1
        );

        assertEquals(UUID_1, dto.getAttachId());
        assertEquals("file.pdf", dto.getAttachName());
        assertTrue(dto.getIsActive());
    }

    @Test
    void getterSetter_Works() {
        AttachDTO dto = new AttachDTO();
        LocalDateTime date = LocalDateTime.now();

        dto.setObjectId(UUID_1);
        dto.setObjectType(1);
        dto.setAttachName("test");
        dto.setAttachPath("/path/to/file");
        dto.setCreatorId(UUID_1);
        dto.setDateCreate(LocalDateTime.now());
        dto.setAttachType(1);
        dto.setSavePath("/save/path");
        dto.setVersionNumber(1L);
        dto.setIsActive(true);
        dto.setIsDeleted(false);
        dto.setCreateAt(date);
        dto.setUpdateAt(date);
        dto.setDeletedAt(date);
        dto.setPartnerId(UUID_1);
        dto.setCreateBy(UUID_1);
        dto.setUpdateBy(UUID_1);
        dto.setDeletedBy(UUID_1);

        assertEquals(UUID_1, dto.getObjectId());
        assertEquals(1, dto.getObjectType());
        assertEquals("test", dto.getAttachName());
        assertEquals("/path/to/file", dto.getAttachPath());
        assertEquals(UUID_1, dto.getCreatorId());
        assertEquals(dto.getDateCreate(),date);
        assertEquals(1, dto.getAttachType());
        assertEquals("/save/path", dto.getSavePath());
        assertEquals(1L, dto.getVersionNumber());
        assertTrue(dto.getIsActive());
        assertFalse(dto.getIsDeleted());
        assertEquals(dto.getCreateAt(),date);
        assertEquals(dto.getUpdateAt(),date);
        assertEquals(dto.getDeletedAt(),date);
        assertEquals(UUID_1, dto.getPartnerId());
        assertEquals(UUID_1, dto.getCreateBy());
        assertEquals(UUID_1, dto.getUpdateBy());
        assertEquals(UUID_1, dto.getDeletedBy());
    }

    @Test
    void builder_CreatesObjectCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 11, 21, 14, 0);

        AttachDTO dto = AttachDTO.builder()
                .attachId(UUID_1)
                .objectId(UUID_2)
                .attachName("test.pdf")
                .attachPath("/uploads/2025/11/contract.pdf")
                .creatorId(UUID_1)
                .dateCreate(now)
                .attachType(1)
                .versionNumber(5L)
                .isActive(true)
                .isDeleted(false)
                .partnerId(UUID_1)
                .createAt(now)
                .build();

        assertEquals("test.pdf", dto.getAttachName());
        assertEquals(1, dto.getAttachType());
        assertEquals(now, dto.getDateCreate());
    }

    @Test
    void equalsAndHashCode_SameFields_Equal() {
        AttachDTO dto1 = AttachDTO.builder().attachId(UUID_1).attachName("file.pdf").build();
        AttachDTO dto2 = AttachDTO.builder().attachId(UUID_1).attachName("file.pdf").build();

        assertEquals(dto1.getAttachName(), dto2.getAttachName());
    }

    @Test
    void equals_DifferentId_NotEqual() {
        AttachDTO dto1 = AttachDTO.builder().attachId(UUID_1).build();
        AttachDTO dto2 = AttachDTO.builder().attachId(UUID_2).build();

        assertNotEquals(dto1, dto2);
    }

}