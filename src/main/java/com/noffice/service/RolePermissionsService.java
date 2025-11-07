package com.noffice.service;

import com.noffice.entity.Permission;
import com.noffice.entity.PermissionRole;
import com.noffice.entity.PermissionRoleId;
import com.noffice.entity.User;
import com.noffice.repository.RoleRepository;
import com.noffice.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noffice.repository.RolePermissionsRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RolePermissionsService {
	@Autowired
	private RolePermissionsRepository rolePermissionsRepository;
	@Autowired
	private LogService logService;
	@Autowired
	private RoleRepository roleRepository;
	
	public List<Permission> getRolePermissions(UUID roleId) {

		return  rolePermissionsRepository.findPermissionsByRoleId(roleId);
	}

	public List<Permission> getRolePermissionsHalf(UUID roleId) {

		return  rolePermissionsRepository.findPermissionsHalfByRoleId(roleId);
	}

	@Transactional
	public void updatePermissionsForRole(UUID roleId, List<UUID> checkedKeys, List<UUID> checkedHalfKeys) {
	    // 1. Xóa tất cả quyền cũ của role
	    rolePermissionsRepository.deleteByRoleId(roleId);

	    // 2. Thêm các quyền mới bằng cách tạo entity với @EmbeddedId
	    List<PermissionRole> newPermissions = checkedKeys.stream()
	        .map(permissionId -> new PermissionRole(new PermissionRoleId( permissionId ,roleId, false)))
	        .collect(Collectors.toList());

		List<PermissionRole> newHalfPermissions = checkedHalfKeys.stream()
				.map(permissionId -> new PermissionRole(new PermissionRoleId( permissionId ,roleId, true)))
				.collect(Collectors.toList());

	    rolePermissionsRepository.saveAll(newPermissions);
		rolePermissionsRepository.saveAll(newHalfPermissions);
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
