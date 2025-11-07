package com.noffice.controller;

import com.noffice.dto.CreateUserGroupDTO;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.GenericPaginationDTO;
import com.noffice.entity.User;
import com.noffice.entity.UserGroup;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.reponse.UserGroupResponse;
import com.noffice.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/userGroups")
public class UserGroupController {
    @Autowired
    private UserGroupService userGroupService;

    @PostMapping("/save")
    public ResponseAPI saveUserGroup(@RequestBody CreateUserGroupDTO payload) {
        try {
            UUID id = null;
            if (payload.getId() != null && !payload.getId().toString().isEmpty()) {
                id = payload.getId();
            }
            UserGroup userGroup = userGroupService.saveUserGroup(id, payload.getGroupName(), payload.getGroupCode(), payload.getUserIds());
            return new ResponseAPI(userGroup, "User group created successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error creating user group: " + e.getMessage(), 400);
        }
    }

    @GetMapping("/search")
    public ResponseAPI getAllUserGroups(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "searchString", required = false, defaultValue = "") String searchString,
            @RequestParam(value = "groupCode", required = false, defaultValue = "") String groupCode,
            @RequestParam(value = "groupName", required = false, defaultValue = "") String groupName,
            @RequestParam(value = "status", required = false) Boolean status
            ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            UUID partnerId = userDetails.getPartnerId();
            Pageable pageable = PageRequest.of(page, size);

            Page<UserGroupResponse> userGroups = userGroupService.searchUserGroups(partnerId, searchString, groupCode, groupName, status, pageable);
            GenericPaginationDTO paginationDTO = new GenericPaginationDTO(userGroups.getTotalElements(), userGroups.getContent());
            return new ResponseAPI(paginationDTO, "User groups retrieved successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error retrieving user groups: " + e.getMessage(), 400);
        }
    }

    @PutMapping("/updateStatus")
    public ResponseAPI updateUserGroupStatus(@RequestParam(value = "id") UUID id,@RequestParam(value = "version") Long version) {
        try {
            String message = userGroupService.updateUserGroupStatus(id, version);
            if (message != null && !message.trim().isEmpty()) {
                return new ResponseAPI(null, message, 400);
            }
            return new ResponseAPI(null, "User group status updated successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error updating user group status: " + e.getMessage(), 400);
        }
    }

    @DeleteMapping("/delete")
    public ResponseAPI deleteUserGroup(@RequestParam(value = "id") UUID id, @RequestParam(value = "version") Long version) {
        try {
            String message = userGroupService.deleteUserGroup(id, version);
            if (message != null && !message.trim().isEmpty()) {
                return new ResponseAPI(null, message, 400);
            }
            return new ResponseAPI(null, "User group deleted successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error deleting user group: " + e.getMessage(), 400);
        }
    }

    @PostMapping("/deleteMultiple")
    public ResponseAPI deleteMultipleUserGroups(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return new ResponseAPI(null, "No user groups to delete", 404);
            }
            for (DeleteMultiDTO groupId : ids) {
                userGroupService.deleteUserGroup(groupId.getId(), groupId.getVersion());
            }
            return new ResponseAPI(null, "User groups deleted successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error deleting user groups: " + e.getMessage(), 400);
        }
    }

    @GetMapping("/logDetail")
    public ResponseAPI getUserGroupLogDetail(@RequestParam(value = "id") UUID groupId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            userGroupService.saveLogDetail(groupId, userDetails);
            return new ResponseAPI(null, "Log details retrieved successfully", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Error retrieving log details: " + e.getMessage(), 400);
        }
    }

    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            ErrorListResponse message = userGroupService.checkDeleteMulti(ids);
            return new ResponseAPI(message, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
}
