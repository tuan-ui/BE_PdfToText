package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.UserGroupResponse;
import com.noffice.repository.UserGroupRepository;
import com.noffice.repository.UserGroupsRepository;
import com.noffice.repository.UserRepository;
import com.noffice.ultils.Constants;
import com.noffice.ultils.StringUtils;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final UserGroupsRepository userGroupsRepository;
    private final UserRepository userRepository;
    private final LogService logService;

    @Transactional
    public UserGroup saveUserGroup(UUID id, String groupName, String groupCode, List<UUID> userIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userCreate = (User) authentication.getPrincipal();
        UserGroup userGroup;
        if (id != null) {
            userGroup = userGroupRepository.findById(id).orElse(new UserGroup());
            userGroup.setUpdateBy(userCreate.getId());
        } else {
            userGroup = new UserGroup();
            userGroup.setCreateBy(userCreate.getId());
            userGroup.setPartnerId(userCreate.getPartnerId());
            userGroup.setIsDeleted(Constants.isDeleted.ACTIVE);
        }
        userGroup.setGroupName(groupName);
        userGroup.setGroupCode(groupCode);
        UserGroup newGroup = userGroupRepository.save(userGroup);
        saveUserGroups(newGroup.getId(), userIds);
        logService.createLog(
                id != null ? ActionType.UPDATE.getAction() : ActionType.CREATE.getAction(),
                Map.of(
                        "actor", userCreate.getFullName(),
                        "action", id != null ? FunctionType.EDIT_USER_GROUP.getFunction() : FunctionType.CREATE_USER_GROUP.getFunction(),
                        "object", newGroup.getGroupName()
                ),
                userCreate.getId(),
                newGroup.getId(),
                userCreate.getPartnerId()
        );
        return newGroup;
    }

    public Page<UserGroupResponse> searchUserGroups(UUID partnerId, String searchString, String groupCode, String groupName, Boolean status, Pageable pageable) {
        String searchStringUnaccented = StringUtils.removeAccents(searchString);
        Page<UserGroup> userGroups = userGroupRepository.searchUserGroup(partnerId, searchStringUnaccented, groupCode, groupName, status, pageable);

        return userGroups.map(ug -> {
            UserGroupResponse response = new UserGroupResponse();
            response.setId(ug.getId());
            response.setGroupName(ug.getGroupName());
            response.setGroupCode(ug.getGroupCode());
            response.setVersion(ug.getVersion());
            response.setIsActive(ug.getIsActive());

            List<UUID> userIds = userGroupsRepository.findUserIdsByGroupId(ug.getId());
            if (!userIds.isEmpty()) {
                List<User> users = userRepository.findAllById(userIds);
                response.setUsers(users.stream().map(user -> {
                    UserGroupResponse.UserResponse ur = new UserGroupResponse.UserResponse();
                    ur.setUserId(user.getId());
                    ur.setUsername(user.getUsername());
                    ur.setUserCode(user.getUserCode());
                    ur.setFullName(user.getFullName());
                    return ur;
                }).collect(Collectors.toList()));
            } else {
                response.setUsers(Collections.emptyList());
            }

            return response;
        });
    }

    public void saveUserGroups(UUID id, List<UUID> userIds) {
        userGroupsRepository.deleteByGroupId(id);

        List<UserGroups> newUserGroups = userIds.stream()
                .map(userIdStr -> new UserGroups(new UserGroupsId(userIdStr, id)))
                .collect(Collectors.toList());
        userGroupsRepository.saveAll(newUserGroups);
    }

    public String updateUserGroupStatus(UUID id,  Long version) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        UserGroup userGroup = userGroupRepository.findByIdIncludeDeleted(id);
        if (!Objects.equals(userGroup.getVersion(), version)) {
            return  "error.DataChangedReload";
        }
        if(userGroup.getIsDeleted())
            return	"error.UserGroupDoesNotExist";
        else {
            userGroup.setIsActive(!userGroup.getIsActive());
            userGroup.setUpdateBy(userDetails.getId());
            userGroupRepository.save(userGroup);
            logService.createLog(
                    userGroup.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of(
                            "actor", userDetails.getFullName(),
                            "action", userGroup.getIsActive() ? FunctionType.UNLOCK_USER_GROUP.getFunction() : FunctionType.LOCK_USER_GROUP.getFunction(),
                            "object", userGroup.getGroupName()
                    ),
                    userDetails.getId(),
                    userGroup.getId(),
                    userDetails.getPartnerId()
            );
        }
        return "";
    }

    @Transactional
    public String deleteUserGroup(UUID id,  Long version) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        UserGroup userGroup = userGroupRepository.findByIdIncludeDeleted(id);
        if (!Objects.equals(userGroup.getVersion(), version)) {
            return  "error.DataChangedReload";
        }
        if(userGroup.getIsDeleted())
            return	"error.UserGroupDoesNotExist";
        else {
            userGroup.setIsDeleted(Constants.isDeleted.DELETED);
            userGroup.setDeletedBy(userDetails.getId());
            userGroupRepository.save(userGroup);
            userGroupsRepository.deleteByGroupId(id);
            logService.createLog(
                    ActionType.DELETE.getAction(),
                    Map.of(
                            "actor", userDetails.getFullName(),
                            "action", FunctionType.DELETE_USER_GROUP.getFunction(),
                            "object", userGroup.getGroupName()
                    ),
                    userDetails.getId(),
                    userGroup.getId(),
                    userDetails.getPartnerId()
            );
        }
        return "";
    }

    public void saveLogDetail(UUID id, User user) {
        UserGroup userGroup = userGroupRepository.findById(id).orElse(null);
        logService.createLog(
                ActionType.VIEW.getAction(),
                Map.of(
                        "actor", user.getFullName(),
                        "action", FunctionType.VIEW_DETAIL_USER_GROUP.getFunction(),
                        "object", userGroup != null ? userGroup.getGroupName() : ""
                ),
                user.getId(),
                userGroup != null ? userGroup.getId() : null,
                user.getPartnerId()
        );
    }

    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for(DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            UserGroup userGroup = userGroupRepository.findByIdIncludeDeleted(id.getId());
            if (userGroupsRepository.existsUserByGroupId(id.getId())) {
                object.setErrorMessage("error.UserGroupAlreadyUseOnUser");
            } else if (userGroup.getIsDeleted()) {
                object.setErrorMessage("error.RoleDoesNotExist");
            }
            object.setCode(userGroup.getGroupCode());
            object.setName(userGroup.getGroupName());
            lstObject.add(object);

        }
        response.setErrors(lstObject);
        response.setTotal(ids.size());
        long countNum = response.getErrors().stream()
                .filter(item -> item.getErrorMessage()!=null)
                .count();
        response.setHasError(countNum != 0);
        if(!response.getHasError())
        {
            return null;
        }
        return response;
    }
}
