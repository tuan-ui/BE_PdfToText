package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.TaskType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.TaskTypeRepository;
import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskTypeService {
    private final TaskTypeRepository taskTypeRepository;
    private final LogService logService;

    @Transactional
    public String deleteTaskType(UUID id, User user, Long version) {
        TaskType taskType = taskTypeRepository.findByTaskTypeIdIncludeDeleted(id);
        if (!Objects.equals(taskType.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (taskType.getIsDeleted())
            return "error.TaskTypeNotExists";
        else {
            taskType.setIsDeleted(Constants.isDeleted.DELETED);
            taskType.setDeletedBy(user.getId());
            taskType.setDeletedAt(LocalDateTime.now());
            TaskType savedTaskType = taskTypeRepository.save(taskType);
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_TASKTYPE.getFunction(), "object", savedTaskType.getTaskTypeName()),
                    user.getId(), savedTaskType.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMultiTaskType(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            TaskType taskType = taskTypeRepository.findByTaskTypeIdIncludeDeleted(id.getId());
            if (!Objects.equals(taskType.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            }
            if (taskType.getIsDeleted()) {
                return "error.TaskTypeNotExists";
            } else {
                taskType.setIsDeleted(Constants.isDeleted.DELETED);
                taskType.setDeletedBy(user.getId());
                taskType.setDeletedAt(LocalDateTime.now());
                TaskType savedTaskType = taskTypeRepository.save(taskType);
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_TASKTYPE.getFunction(), "object", savedTaskType.getTaskTypeName()),
                        user.getId(), savedTaskType.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlockTaskType(UUID id, User user, Long version) {
        TaskType taskType = taskTypeRepository.findByTaskTypeIdIncludeDeleted(id);
        if (!Objects.equals(taskType.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (taskType.getIsDeleted())
            return "error.TaskTypeNotExists";
        else {
            Boolean newStatus = !taskType.getIsActive();
            taskType.setIsActive(newStatus);

            taskType.setUpdateBy(user.getId());
            taskType.setUpdateAt(LocalDateTime.now());
            TaskType savedTaskType = taskTypeRepository.save(taskType);
            logService.createLog(savedTaskType.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", savedTaskType.getIsActive() ? FunctionType.UNLOCK_TASKTYPE.getFunction() : FunctionType.LOCK_TASKTYPE.getFunction(), "object", savedTaskType.getTaskTypeName()),
                    user.getId(), savedTaskType.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String saveTaskType(TaskType taskTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        if (taskTypeRepository.findByCode(taskTypeDTO.getTaskTypeCode(), token.getPartnerId()) == null) {
            TaskType taskType = new TaskType();
            taskType.setTaskTypeName(taskTypeDTO.getTaskTypeName());
            taskType.setTaskTypeCode(taskTypeDTO.getTaskTypeCode());
            taskType.setTaskTypeDescription(taskTypeDTO.getTaskTypeDescription());
            taskType.setTaskTypePriority(taskTypeDTO.getTaskTypePriority());
            taskType.setCreateAt(LocalDateTime.now());
            taskType.setCreateBy(token.getId());
            taskType.setIsActive(taskTypeDTO.getIsActive());
            taskType.setIsDeleted(Constants.isDeleted.ACTIVE);
            taskType.setPartnerId(token.getPartnerId());
            TaskType savedTaskType = taskTypeRepository.save(taskType);
            logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_TASKTYPE.getFunction(), "object", savedTaskType.getTaskTypeName()),
                    token.getId(), savedTaskType.getId(), token.getPartnerId());

            return "";
        } else {
            return "error.TaskTypeNotExists";
        }
    }

    @Transactional
    public String updateTaskType(TaskType taskTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        TaskType taskType = taskTypeRepository.findByTaskTypeIdIncludeDeleted(taskTypeDTO.getId());
        if (!Objects.equals(taskType.getVersion(), taskTypeDTO.getVersion())) {
            return "error.DataChangedReload";
        }
        if (taskType.getIsDeleted())
            return "error.TaskTypeNotExists";
        else {
            taskType.setTaskTypeName(taskTypeDTO.getTaskTypeName());
            taskType.setTaskTypeCode(taskTypeDTO.getTaskTypeCode());
            taskType.setTaskTypeDescription(taskTypeDTO.getTaskTypeDescription());
            taskType.setTaskTypePriority(taskTypeDTO.getTaskTypePriority());
            taskType.setIsActive(taskTypeDTO.getIsActive());
            taskType.setPartnerId(token.getPartnerId());
            taskType.setUpdateAt(LocalDateTime.now());
            taskType.setUpdateBy(token.getId());
            TaskType savedTaskType = taskTypeRepository.save(taskType);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_TASKTYPE.getFunction(), "object", savedTaskType.getTaskTypeName()),
                    token.getId(), savedTaskType.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<TaskType> getListTaskType(String searchString, String taskTypeCode, String taskTypeName, String taskTypeDescription,
                                        Pageable pageable, UUID partnerId) {
        return taskTypeRepository.getTaskTypeWithPagination(searchString, taskTypeCode, taskTypeName, taskTypeDescription, partnerId, pageable);
    }

    public List<TaskType> getAllTaskType(UUID partnerId) {
        return taskTypeRepository.getAllTaskType(partnerId);
    }

    public void getLogDetailTaskType(String id, User user) {
        TaskType taskType = taskTypeRepository.findByTaskTypeCode(id);
        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.VIEW_DETAIL_TASKTYPE.getFunction(), "object", taskType.getTaskTypeName()),
                user.getId(), taskType.getId(), user.getPartnerId());
    }


    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for (DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            TaskType taskType = taskTypeRepository.findByTaskTypeIdIncludeDeleted(id.getId());
            if (!Objects.equals(taskType.getVersion(), id.getVersion())) {
                object.setErrorMessage("error.DataChangedReload");
            } else if (taskType.getIsDeleted()) {
                object.setErrorMessage("error.TaskTypeNotExists");
            }
            object.setCode(taskType.getTaskTypeCode());
            object.setName(taskType.getTaskTypeName());
            lstObject.add(object);
        }
        response.setErrors(lstObject);
        response.setTotal(ids.size());
        long countNum = response.getErrors().stream()
                .filter(item -> item.getErrorMessage() != null)
                .count();
        response.setHasError(countNum != 0);
        if (!response.getHasError()) {
            return null;
        }
        return response;
    }
}
