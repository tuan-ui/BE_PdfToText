package com.noffice.service;

import com.noffice.entity.*;
import com.noffice.repository.RoleRepository;

import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.noffice.repository.RolePermissionsRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionsService {
	private final RolePermissionsRepository rolePermissionsRepository;
	private final LogService logService;
	private final RoleRepository roleRepository;
	
	public List<Permission> getRolePermissions(UUID roleId) {

		return  rolePermissionsRepository.findPermissionsByRoleId(roleId);
	}

	public List<Permission> getRolePermissionsHalf(UUID roleId) {

		return  rolePermissionsRepository.findPermissionsHalfByRoleId(roleId);
	}

	@Transactional
	public String updatePermissionsForRole(UUID roleId, List<UUID> checkedKeys, List<UUID> checkedHalfKeys) {

		Role role = roleRepository.findByRoleIdIncluideDeleted(roleId);
		if (role == null) {
			return  Constants.errorResponse.DATA_CHANGED;
		} else {
			// 1. Xóa tất cả quyền cũ của role
			rolePermissionsRepository.deleteByRoleId(roleId);

			// 2. Thêm các quyền mới bằng cách tạo entity với @EmbeddedId
			List<PermissionRole> newPermissions = checkedKeys.stream()
					.map(permissionId -> new PermissionRole(new PermissionRoleId(permissionId, roleId, false)))
					.toList();

			List<PermissionRole> newHalfPermissions = checkedHalfKeys.stream()
					.map(permissionId -> new PermissionRole(new PermissionRoleId(permissionId, roleId, true)))
					.toList();

			rolePermissionsRepository.saveAll(newPermissions);
			rolePermissionsRepository.saveAll(newHalfPermissions);
		}
		return "";
	}

	public List<Permission> getUserPermissions(User user) {

		return  rolePermissionsRepository.findPermissionsByUserID(user.getId());
	}

	public List<Permission> getUserOriginDataPermissions(String code, User user) {

		return  rolePermissionsRepository.findChildrenByParentCodeAndUserId(code, user.getId());
	}
	public List<String> getPermissionsCurrent(String code, User user) {

		return  rolePermissionsRepository.getPermissionsCurrent(code, user.getId());
	}

}
