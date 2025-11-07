package com.noffice.repository;

import com.noffice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRolesRepository extends JpaRepository<UserRoles, UserRolesId> {

	@Modifying
	@Query("DELETE FROM UserRoles pr WHERE pr.id.userId = :userId")
	void deleteByUserId(@Param("userId") UUID userId);

	@Query("Select pr.id.roleId FROM UserRoles pr WHERE pr.id.userId = :userId")
	List<UUID> getRolesByUserId(@Param("userId") UUID userId);
	@Query("SELECT COUNT(pr) > 0 FROM UserRoles pr WHERE pr.id.roleId = :roleId")
	boolean existsUserByRoleId(@Param("roleId") UUID roleId);

}
