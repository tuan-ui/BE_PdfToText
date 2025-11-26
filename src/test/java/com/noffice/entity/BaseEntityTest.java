package com.noffice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    static class TestEntity extends BaseEntity {
    }

    private TestEntity entity;

    @BeforeEach
    void setUp() {
        entity = new TestEntity();
    }

    @Test
    void testGetterSetterFields() {
        entity.setVersion(5L);
        assertEquals(5L, entity.getVersion());
        UUID partnerId = UUID.randomUUID();
        entity.setPartnerId(partnerId);
        assertEquals(partnerId, entity.getPartnerId());
        UUID createBy = UUID.randomUUID();
        entity.setCreateBy(createBy);
        assertEquals(createBy, entity.getCreateBy());
        UUID updateBy = UUID.randomUUID();
        entity.setUpdateBy(updateBy);
        assertEquals(updateBy, entity.getUpdateBy());
        UUID deletedBy = UUID.randomUUID();
        entity.setDeletedBy(deletedBy);
        assertEquals(deletedBy, entity.getDeletedBy());
    }

    @Test
    void testOnCreate_allNull() {
        entity.onCreate();

        assertNotNull(entity.getId());
        assertNotNull(entity.getIsActive());
        assertNotNull(entity.getIsDeleted());
        assertNotNull(entity.getCreateAt());
        assertNotNull(entity.getUpdateAt());
    }

    @Test
    void testOnCreate_idNotNull_createAtUpdateAtNull() {
        UUID existingId = UUID.randomUUID();
        entity.setId(existingId);

        entity.onCreate();
        assertEquals(existingId, entity.getId());

        assertNotNull(entity.getIsActive());
        assertNotNull(entity.getIsDeleted());
        assertNotNull(entity.getCreateAt());
        assertNotNull(entity.getUpdateAt());
    }

    @Test
    void testOnCreate_allNotNull() {
        UUID existingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(existingId);
        entity.setCreateAt(now);
        entity.setUpdateAt(now);
        entity.setIsActive(true);
        entity.setIsDeleted(false);
        entity.onCreate();

        assertEquals(existingId, entity.getId());
        assertEquals(true,entity.getIsActive());
        assertEquals(false,entity.getIsDeleted());
        assertEquals(now, entity.getCreateAt());
        assertEquals(now, entity.getUpdateAt());
    }
    @Test
    void testOnUpdate_updateAtChanges() {
        LocalDateTime oldUpdateAt = LocalDateTime.now().minusDays(1);
        entity.setUpdateAt(oldUpdateAt);

        entity.onUpdate();

        assertNotNull(entity.getUpdateAt());
        assertTrue(entity.getUpdateAt().isAfter(oldUpdateAt));
    }

    @Test
    void testOnUpdate_deletedTrueDeletedAtNull() {
        entity.setIsDeleted(true);
        assertNull(entity.getDeletedAt());

        entity.onUpdate();

        assertNotNull(entity.getDeletedAt());
    }

    @Test
    void testOnUpdate_deletedTrueDeletedAtNotNull() {
        LocalDateTime deletedTime = LocalDateTime.now().minusHours(1);
        entity.setIsDeleted(true);
        entity.setDeletedAt(deletedTime);

        entity.onUpdate();

        assertEquals(deletedTime, entity.getDeletedAt());
    }

    @Test
    void testOnUpdate_deletedFalse() {
        entity.setIsDeleted(false);
        entity.setDeletedAt(null);

        entity.onUpdate();

        assertNull(entity.getDeletedAt());
    }
}
