package com.noffice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.RolePermissionRequest;
import com.noffice.entity.Permission;
import com.noffice.entity.Role;
import com.noffice.service.RolePermissionsService;
import com.noffice.reponse.ErrorListResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.noffice.dto.RoleDTO;
import com.noffice.dto.RoleSearchDTO;
import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.RoleService;
import com.noffice.ultils.StringUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Tag(name = "RoleController", description = "Quản lý Vai trò")
public class RoleController {
	private final RoleService roleService;
    private final RolePermissionsService rolePermissionsService;

	@Operation(summary = "Lấy danh sách vai trò", description = "Trả về danh sách các vai trò dựa trên tiêu chí tìm kiếm")
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "Kết quả trả về(200: Thành công, >= 400: Dữ liệu không hợp lệ)",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": {}, \"message\": \"Thành công\", \"status\": 200}"))),
	    @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": null, \"message\": \"Lỗi hệ thống\", \"status\": 500}")))
	})
	@PostMapping("/searchRoles")
	public ResponseEntity<ResponseAPI> searchRoles(@RequestBody @Parameter(description = "Thông tin tìm kiếm vai trò") RoleSearchDTO request) {
	    try {
	    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			
			if (request.getSearchString() != null && !request.getSearchString().trim().isEmpty()) {
	            String textEscape = StringUtils.unAccent(request.getSearchString()).trim();
	            textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
	            request.setSearchString(textEscape);
	        }
	    	if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
	            String textEscape = StringUtils.unAccent(request.getRoleName()).trim();
	            textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
	            request.setRoleName(textEscape);
	        }
	    	if (request.getRoleCode() != null && !request.getRoleCode().isEmpty()) {
	            String textEscape = StringUtils.unAccent(request.getRoleCode()).trim();
	            textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
	            request.setRoleCode(textEscape);
	        }
	    	if (request.getRoleDescription() != null && !request.getRoleDescription().isEmpty()) {
	            String textEscape = StringUtils.unAccent(request.getRoleDescription()).trim();
	            textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
	            request.setRoleDescription(textEscape);
	        }

			Page<RoleDTO> result = roleService.searchRoles(
        		request.getSearchString(),
        	    request.getRoleName(),
        	    request.getRoleCode(),
        	    request.getRoleDescription(),
				userDetails.getPartnerId(),
        	    request.getStatus(),
        	    request.getPage(),
        	    request.getSize()
        	);

	        return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(result, "Thành công", 200));
	    } catch (Exception e) {
	    	System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
	    }
	}
	
	@Operation(summary = "Thêm vai trò mới", description = "API này dùng để thêm một vai trò mới vào hệ thống")
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "Kết quả trả về(200: Thành công, >= 400: Dữ liệu không hợp lệ)",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": {}, \"message\": \"Thành công\", \"status\": 200}"))),
	    @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": null, \"message\": \"Lỗi hệ thống\", \"status\": 500}")))
	})
    @PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@RequestBody @Valid @Parameter(description = "Dữ liệu vai trò cần thêm") RoleDTO roleDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			
			String message = roleService.save(roleDTO, userDetails);
			if(org.apache.commons.lang3.StringUtils.isNotBlank(message))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, message, 400));
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@Operation(summary = "Cập nhật vai trò", description = "Cập nhật thông tin của vai trò trong hệ thống")
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "Kết quả trả về(200: Thành công, >= 400: Dữ liệu không hợp lệ)",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": {}, \"message\": \"Thành công\", \"status\": 200}"))),
	    @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": null, \"message\": \"Lỗi hệ thống\", \"status\": 500}")))
	})
    @PostMapping("/update")
	public ResponseEntity<ResponseAPI> update(@RequestBody @Parameter(description = "Thông tin vai trò cần cập nhật") RoleDTO roleDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			
			String message = roleService.update(roleDTO, userDetails);
			if(org.apache.commons.lang3.StringUtils.isNotBlank(message))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, message, 400));
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@Operation(summary = "Xóa vai trò", description = "Xóa một vai trò dựa trên ID và role_user_dept_id")
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "Kết quả trả về(200: Thành công, >= 400: Dữ liệu không hợp lệ)",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": {}, \"message\": \"Thành công\", \"status\": 200}"))),
	    @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": null, \"message\": \"Lỗi hệ thống\", \"status\": 500}")))
	})
	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> delete(
	        @RequestParam(value = "id") UUID id,
			@RequestParam(value = "version") Long version) {
	    try {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        User userDetails = (User) authentication.getPrincipal();

	        String message = roleService.delete(id, userDetails, version);

			if(org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(new ResponseAPI(null, message, 400));
	        }

	        return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
	    } catch (Exception e) {
	        System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
	    }
	}
	
	@Operation(summary = "Khóa vai trò", description = "Vô hiệu hóa một vai trò trong hệ thống dựa trên ID vai trò và ID của role_user_dept.")
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "Kết quả trả về(200: Thành công, >= 400: Dữ liệu không hợp lệ)",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": {}, \"message\": \"Thành công\", \"status\": 200}"))),
	    @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(example = "{\"object\": null, \"message\": \"Lỗi hệ thống\", \"status\": 500}")))
	})
	@GetMapping("/lock")
	public ResponseEntity<ResponseAPI> lock(
	        @RequestParam(value = "id") UUID id,
			@RequestParam(value = "version") Long version) {
	    try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
	        String message = roleService.lockRole(id,userDetails, version);

			if(org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(new ResponseAPI(null, message, 400));
	        }

	        return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
	    }
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMuti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();

			String message = roleService.deleteMuti(ids, userDetails);

			if(org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseAPI(null, message, 400));
			}

			return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}


	@GetMapping("/getAllRole")
	public ResponseAPI getAllRole() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<Role> roles = roleService.getAllRole(userDetails.getPartnerId());
			return new ResponseAPI(roles, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}
	@GetMapping("/LogDetailRole")
	public ResponseAPI getLogDetailRole(@RequestParam UUID id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			roleService.getLogDetailRole(id, userDetails);
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}
	@GetMapping("/getALlPermisstion")
	public ResponseAPI getALlPermisstion() {
		try {
			List<Permission> response = roleService.getALlPermisstion();
			return new ResponseAPI(response, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

	@GetMapping("/getRolePermisstion")
	public ResponseAPI getRolePermissions(@RequestParam UUID roleId) {
		List<Permission> response = rolePermissionsService.getRolePermissions(roleId);
		return new ResponseAPI(response, "success", 200);
	}

	@GetMapping("/getRolePermissionsHalf")
	public ResponseAPI getRolePermissionsHalf(@RequestParam UUID roleId) {
		List<Permission> response = rolePermissionsService.getRolePermissionsHalf(roleId);
		return new ResponseAPI(response, "success", 200);
	}

	// Gán lại danh sách permission cho role
	@PostMapping("/updateRolePermisstion")
	public ResponseAPI updateRolePermissions(@RequestBody RolePermissionRequest res) {

		try {
			String message = rolePermissionsService.updatePermissionsForRole(res.getRoleId(), res.getCheckedKeys(), res.getCheckedHalfKeys());
			if(org.apache.commons.lang3.StringUtils.isNotBlank(message)) {
				return new ResponseAPI(null, message, 400);
			}
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}


	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = roleService.checkDeleteMulti(ids);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

	@GetMapping("/getUserPermissions")
	public ResponseAPI getUserPermissions() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User userDetails = (User) authentication.getPrincipal();
		List<Permission> response = rolePermissionsService.getUserPermissions(userDetails);
		return new ResponseAPI(response, "success", 200);
	}

	@GetMapping("/getUserOriginDataPermissions")
	public ResponseAPI getUserOriginDataPermissions() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User userDetails = (User) authentication.getPrincipal();
		List<Permission> response = rolePermissionsService.getUserOriginDataPermissions("ORIGINDATA",userDetails);
		return new ResponseAPI(response, "success", 200);
	}
	@GetMapping("/getPermissionsCurrent")
	public ResponseEntity<?> getPermissionsCurrent(@RequestParam String menuCode) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User userDetails = (User) authentication.getPrincipal();
		List<String> userPermissions = rolePermissionsService
				.getPermissionsCurrent(menuCode, userDetails);

		Map<String, Boolean> pagePerm = new HashMap<>();
		pagePerm.put("add", userPermissions.contains(menuCode + "_ADD"));
		pagePerm.put("edit", userPermissions.contains(menuCode + "_EDIT"));
		pagePerm.put("delete", userPermissions.contains(menuCode + "_DELETE"));
		pagePerm.put("permission", userPermissions.contains(menuCode + "_PERMISSION"));

		return ResponseEntity.ok(pagePerm);
	}

}
