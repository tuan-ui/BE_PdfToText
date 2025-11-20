package com.noffice.service;

import com.noffice.entity.*;
import com.noffice.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRolesService {
	private final  UserRolesRepository userRolesRepository;

	@Transactional
	public void saveUserRoles(UUID userId, List<UUID> roleIds) {
		userRolesRepository.deleteByUserId(userId);

	    List<UserRoles> newPermissions = roleIds.stream()
	        .map(roleId -> new UserRoles(new UserRolesId( userId ,roleId)))
	        .collect(Collectors.toList());

		userRolesRepository.saveAll(newPermissions);
	}
}
