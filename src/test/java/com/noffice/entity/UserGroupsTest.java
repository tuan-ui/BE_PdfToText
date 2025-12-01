package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserGroupsTest {

    @Test
    void testUserGroupsId_GettersAndSetters() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id = new UserGroupsId();
        id.setUserId(userId);
        id.setGroupId(groupId);

        assertThat(id.getUserId()).isEqualTo(userId);
        assertThat(id.getGroupId()).isEqualTo(groupId);
    }

    @Test
    void testUserGroupsId_AllArgsConstructor() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id = new UserGroupsId(userId, groupId);

        assertThat(id.getUserId()).isEqualTo(userId);
        assertThat(id.getGroupId()).isEqualTo(groupId);
    }

    @Test
    void testUserGroupsId_EqualsAndHashCode() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id1 = new UserGroupsId(userId, groupId);
        UserGroupsId id2 = new UserGroupsId(userId, groupId);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void testUserGroups_GettersAndSetters() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id = new UserGroupsId(userId, groupId);
        UserGroups pr = new UserGroups();
        pr.setId(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testUserGroups_AllArgsConstructor() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id = new UserGroupsId(userId, groupId);
        UserGroups pr = new UserGroups(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testToStringIncludesFields() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UserGroupsId id = new UserGroupsId(userId, groupId);
        UserGroups pr = new UserGroups(id);

        String str = pr.toString();
        assertThat(str).contains(userId.toString());
        assertThat(str).contains(groupId.toString());
    }
}
