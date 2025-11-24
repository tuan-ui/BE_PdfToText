package com.noffice.service;

import java.time.LocalDateTime;
import java.util.*;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.Permission;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.repository.PermissionsRepository;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.UserRolesRepository;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.noffice.dto.RoleDTO;
import com.noffice.entity.Role;
import com.noffice.repository.RoleRepository;
import com.noffice.ultils.Constants;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {
	private final RoleRepository roleRepository;
	private final LogService logService;
    private final UserRolesRepository userRolesRepository;
    private final PermissionsRepository permissionsRepository;

	public Page<RoleDTO> searchRoles(String searchString, String roleName, String roleCode, String roleDescription,
									   UUID partnerId, Boolean status, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		if (searchString != null && !searchString.trim().isEmpty()) {
			roleName = null;
			roleCode = null;
			roleDescription = null;
		}
        return roleRepository.searchRoles(searchString, roleName, roleCode, roleDescription,
                partnerId, status, pageable);
	}

	@Transactional
	public String save(RoleDTO roleDTO, User token) {
		if (roleDTO.getRoleCode() != null && roleRepository.existsByRoleCodeIgnoreCase(roleDTO.getRoleCode(), token.getPartnerId())) {
            return "error.SameRoleCode";
        }
		
		Role role = new Role();
		role.setRoleCode(roleDTO.getRoleCode());
		role.setRoleName(roleDTO.getRoleName());
		role.setRoleDescription(roleDTO.getRoleDescription());
		role.setPriority(roleDTO.getPriority());
		role.setPartnerId(token.getPartnerId());
		role.setIsActive(true);
		role.setIsDeleted(false);
		role.setCreateAt(LocalDateTime.now());
		role.setCreateBy(token.getId());
		role.setUpdateAt(LocalDateTime.now());
		role.setUpdateBy(token.getId());
		Role savedRole = roleRepository.save(role);
		logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(),"action", FunctionType.CREATE_ROLE.getFunction(), "object", savedRole.getRoleName()),
				token.getId(), savedRole.getId(),token.getPartnerId());
		return "";
	}

	@Transactional
	public String update(RoleDTO roleDTO, User token) {
		Role role = roleRepository.findByRoleIdIncluideDeleted(roleDTO.getId());
		if (role == null || !Objects.equals(role.getVersion(), roleDTO.getVersion())) {
			return  "error.DataChangedReload";
		} else {
			if (roleDTO.getRoleCode() != null && roleRepository.existsByRoleCodeIgnoreCaseNotId(roleDTO.getRoleCode(), role.getId(), token.getPartnerId())) {
	            return "error.SameRoleCode";
	        }
			role.setRoleCode(roleDTO.getRoleCode());
			role.setRoleName(roleDTO.getRoleName());
			role.setRoleDescription(roleDTO.getRoleDescription());
			role.setPriority(roleDTO.getPriority());
			role.setPartnerId(token.getPartnerId());
			role.setUpdateAt(LocalDateTime.now());
			role.setUpdateBy(token.getId());
			Role savedRole = roleRepository.save(role);
			logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_ROLE.getFunction(), "object", savedRole.getRoleName()),
					token.getId(), savedRole.getId(), token.getPartnerId());
		}
        return "";
	}

	@Transactional
	public String delete(UUID id, User user, Long version) {
		Role role = roleRepository.findByRoleIdIncluideDeleted(id);
		if (role == null || !Objects.equals(role.getVersion(), version)) {
			return  "error.DataChangedReload";
		} else {
			if (userRolesRepository.existsUserByRoleId(role.getId())) {
				return "error.RoleAlreadyUseOnUser";
			}roleRepository.deleteRoleByRoleId(id);
			logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_ROLE.getFunction(), "object", role.getRoleName()),
					user.getId(), role.getId(), user.getPartnerId());
		}
        return "";
	}

	@Transactional
	public String lockRole(UUID id, User userDetails, Long version) {
		Role role = roleRepository.findByRoleIdIncluideDeleted(id);
		if (role == null || !Objects.equals(role.getVersion(), version)) {
			return  "error.DataChangedReload";
		} else {
			role.setIsActive(!role.getIsActive());
			role.setUpdateBy(userDetails.getId());
			roleRepository.save(role);
			logService.createLog(
					role.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
					Map.of(
							"actor", userDetails.getFullName(),
							"action", role.getIsActive() ? FunctionType.UNLOCK_ROLE.getFunction() : FunctionType.LOCK_ROLE.getFunction(),
							"object", role.getRoleName()
					),
					userDetails.getId(),
					role.getId(),
					userDetails.getPartnerId()
			);
		}
        return "";
    }

	@Transactional
	public String deleteMuti(List<DeleteMultiDTO> ids, User user) {

		for(DeleteMultiDTO id :ids){
			Role role = roleRepository.findByRoleIdIncluideDeleted(id.getId());
			if (role == null || !Objects.equals(role.getVersion(), id.getVersion())) {
				return  "error.DataChangedReload";
			} else {
				if (userRolesRepository.existsUserByRoleId(role.getId())) {
					return "error.RoleAlreadyUseOnUser";
				}
				roleRepository.deleteRoleByRoleId(id.getId());
				logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_ROLE.getFunction(), "object", role.getRoleName()),
						user.getId(), role.getId(), user.getPartnerId());
			}
        }
		return "";
	}
	public List<Role> getAllRole(UUID partnerId) {
		return roleRepository.findByOnlyPartnerId(partnerId);
	}


	public void getLogDetailRole(UUID id, User user) {
		Role role = roleRepository.findByRoleId(id)
				.orElseThrow(() -> new EntityNotFoundException("error.RoleCodeNotExists"));
		logService.createLog(
				ActionType.VIEW.getAction(),
				Map.of(
						"actor", user.getFullName(),
						"action", FunctionType.VIEW_DETAIL_ROLE.getFunction(),
						"object", role != null ? role.getRoleName() : ""
				),
				user.getId(),
				role != null ? role.getId() : null,
				user.getPartnerId()
		);
	}

	public List<Permission> getALlPermisstion() {
		return permissionsRepository.findAllByIsDeletedFalseOrderByPositionAsc();
	}

	@Transactional
	public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
		ErrorListResponse response = new ErrorListResponse();
		List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
		for(DeleteMultiDTO id : ids) {
			ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
			object.setId(id.getId());
			Role role = roleRepository.findByRoleIdIncluideDeleted(id.getId());
			if(role == null) {
				object.setErrorMessage("error.DataChangedReload");
				object.setCode(id.getCode());
				object.setName(id.getName());
			} else if (userRolesRepository.existsUserByRoleId(role.getId())) {
				object.setErrorMessage("error.RoleAlreadyUseOnUser");
				object.setCode(role.getRoleCode());
				object.setName(role.getRoleName());
			}   else {
				object.setCode(role.getRoleCode());
				object.setName(role.getRoleName());
			}
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
