package com.noffice.service;

import com.noffice.entity.*;
import com.noffice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserRolesService {
	@Autowired
	private UserRolesRepository userRolesRepository;

	@Transactional
	public void saveUserRoles(UUID userId, List<UUID> roleIds) {
		userRolesRepository.deleteByUserId(userId);

	    List<UserRoles> newPermissions = roleIds.stream()
	        .map(roleId -> new UserRoles(new UserRolesId( userId ,roleId)))
	        .collect(Collectors.toList());

		userRolesRepository.saveAll(newPermissions);
	}
}
