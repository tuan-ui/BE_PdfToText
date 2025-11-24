package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.TaskType;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.TaskTypeService;
import com.noffice.ultils.FileUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/task-type")
@Tag(name = "TaskTypeController", description = "Quản lý Loại Công việc")
public class TaskTypeController {
    private final TaskTypeService taskTypeService;

    @GetMapping("/search")
    public ResponseAPI search(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "taskTypeCode", required = false) String taskTypeCode,
            @RequestParam(value = "taskTypeName", required = false) String taskTypeName,
            @RequestParam(value = "taskTypeDescription", required = false) String taskTypeDescription,
            @RequestParam(value = "searchString", required = false) String searchString) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            String taskTypNameStr = FileUtils.removeAccent(taskTypeName);
            String searchStringStr = FileUtils.removeAccent(searchString);
            String taskTypeDescriptionStr = FileUtils.removeAccent(taskTypeDescription);
            Page<TaskType> taskTypes = taskTypeService.getListTaskType(searchStringStr, taskTypeCode, taskTypNameStr,taskTypeDescriptionStr, pageable, userDetails.getPartnerId());
            return new ResponseAPI(taskTypes, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseAPI> deleteTaskType(@RequestParam(value = "id") UUID id,
                                                     @RequestParam(value = "version") Long version,
                                                     HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String result = taskTypeService.deleteTaskType(id, token, version);
            if(StringUtils.isNotBlank(result))
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/deleteMulti")
    public ResponseEntity<ResponseAPI> deleteMultiTaskType(@RequestBody List<DeleteMultiDTO> id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String result = taskTypeService.deleteMultiTaskType(id, token);
            if(StringUtils.isNotBlank(result))
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/lock")
    public ResponseEntity<ResponseAPI> lockUser(@RequestParam UUID id,
                                                @RequestParam(value = "version") Long version,
                                                HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();

            String result = taskTypeService.lockUnlockTaskType(id,token, version);
            if(StringUtils.isNotBlank(result))
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseAPI> save(@Valid @RequestBody TaskType taskType, HttpServletRequest request) { // Thêm @Valid
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String result = taskTypeService.saveTaskType(taskType, authentication);
            if(StringUtils.isNotBlank(result))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseAPI> updateTaskType(@Valid @RequestBody TaskType taskType, HttpServletRequest request) { // Thêm @Valid
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            String result = taskTypeService.updateTaskType(taskType, authentication);

            if(StringUtils.isNotBlank(result))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @GetMapping("/getAllTaskType")
    public ResponseAPI getAllTaskType() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            List<TaskType> taskTypes = taskTypeService.getAllTaskType(userDetails.getPartnerId());
            return new ResponseAPI(taskTypes, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @GetMapping("/LogDetailTaskType")
    public ResponseAPI getLogDetailTaskType(@RequestParam String id) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            taskTypeService.getLogDetailTaskType(id, userDetails);
            return new ResponseAPI(null, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            ErrorListResponse message = taskTypeService.checkDeleteMulti(ids);
            return new ResponseAPI(message, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
}
