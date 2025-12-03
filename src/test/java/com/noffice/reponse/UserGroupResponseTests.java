package com.noffice.reponse;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserGroupResponseTests {

    @Test
    void test_UserGroupResponse_Builder_And_Nested_UserResponse() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserGroupResponse.UserResponse user = UserGroupResponse.UserResponse.builder()
                .userId(userId)
                .username("admin")
                .userCode("U001")
                .fullName("Nguyen Van A")
                .build();

        List<UserGroupResponse.UserResponse> users = Collections.singletonList(user);

        UserGroupResponse response = UserGroupResponse.builder()
                .id(groupId)
                .groupName("Admin Group")
                .groupCode("ADM")
                .isActive(true)
                .version(1L)
                .users(users)
                .build();

        assertThat(response.getId()).isEqualTo(groupId);
        assertThat(response.getGroupName()).isEqualTo("Admin Group");
        assertThat(response.getGroupCode()).isEqualTo("ADM");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getVersion()).isEqualTo(1L);
        assertThat(response.getUsers()).hasSize(1);
        assertThat(response.getUsers().get(0).getUsername()).isEqualTo("admin");
        assertThat(response.getUsers().get(0).getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    void test_UserGroupResponse_UserResponse_NoArgsConstructor() {
        UserGroupResponse.UserResponse user = new UserGroupResponse.UserResponse();
        user.setUserId(UUID.randomUUID());
        user.setUsername("testuser");

        assertThat(user.getUsername()).isEqualTo("testuser");
    }
}