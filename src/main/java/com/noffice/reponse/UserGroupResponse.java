package com.noffice.reponse;

import com.noffice.dto.UserDetailDTO;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class UserGroupResponse {
    private UUID id;
    private String groupName;
    private String groupCode;
    private Boolean isActive;
    private Long version;
    private List<UserResponse> users;

    @Data
    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    public static class UserResponse {
        private UUID userId;
        private String username;
        private String userCode;
        private String fullName;
    }
}
