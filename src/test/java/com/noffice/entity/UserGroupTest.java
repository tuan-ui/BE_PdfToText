package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserGroupTest {

    @Test
    void testGettersAndSetters() {
        UUID commonId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserGroup group = new UserGroup();
        group.setId(commonId);
        group.setGroupCode("GRP001");
        group.setGroupName("Admin Group");
        group.setIsActive(true);
        group.setIsDeleted(false);
        group.setCreateAt(now);
        group.setUpdateAt(now);

        assertThat(group.getId()).isEqualTo(commonId);
        assertThat(group.getGroupCode()).isEqualTo("GRP001");
        assertThat(group.getGroupName()).isEqualTo("Admin Group");
        assertThat(group.getIsActive()).isTrue();
        assertThat(group.getIsDeleted()).isFalse();
        assertThat(group.getCreateAt()).isEqualTo(now);
        assertThat(group.getUpdateAt()).isEqualTo(now);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        UserGroup group = new UserGroup();
        group.setId(UUID.randomUUID());
        group.setGroupCode("GRP001");
        group.setGroupName("Admin Group");
        group.setIsActive(true);
        group.setIsDeleted(false);
        group.setCreateAt(now);
        group.setUpdateAt(now);

        assertThat(group.getGroupCode()).isEqualTo("GRP001");
        assertThat(group.getGroupName()).isEqualTo("Admin Group");
    }

    @Test
    void testBuilder() {
        UserGroup group = UserGroup.builder()
                .groupCode("GRP001")
                .groupName("Admin Group")
                .build();

        assertThat(group.getGroupCode()).isEqualTo("GRP001");
        assertThat(group.getGroupName()).isEqualTo("Admin Group");
    }
}
