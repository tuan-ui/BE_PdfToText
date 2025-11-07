package com.noffice.repository;

import java.util.List;
import java.util.UUID;

import com.noffice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.noffice.entity.PermissionRole;
import com.noffice.entity.PermissionRoleId;
import com.noffice.entity.Role;

public interface RolePermissionsRepository extends JpaRepository<PermissionRole, PermissionRoleId> {


	@Query("""
		SELECT p.permissionCode
		FROM PermissionRole pr
		JOIN Permission p ON pr.id.permissionId = p.id
		JOIN UserRoles ru ON ru.id.roleId = pr.id.roleId
		WHERE ru.id.userId = :userId
		  AND p.isDeleted = false
			  AND p.isMenus = true
	""")
	List<String> findPermissionsByRoleIds(@Param("userId") UUID userId);

	@Query(value ="SELECT p " +
	           "FROM PermissionRole pr " +
	           "JOIN Permission p ON pr.id.permissionId = p.id " +
	           "WHERE pr.id.roleId = :roleId AND p.isDeleted = false  And pr.id.isHalf = false")
	List<Permission> findPermissionsByRoleId(@Param("roleId") UUID roleId);

	@Query(value ="SELECT p " +
			"FROM PermissionRole pr " +
			"JOIN Permission p ON pr.id.permissionId = p.id " +
			"WHERE pr.id.roleId = :roleId AND p.isDeleted = false  And pr.id.isHalf = true")
	List<Permission> findPermissionsHalfByRoleId(@Param("roleId") UUID roleId);

	@Query("""
    SELECT p
    FROM PermissionRole pr
    JOIN Permission p ON pr.id.permissionId = p.id
    JOIN UserRoles ru ON ru.id.roleId = pr.id.roleId
    WHERE ru.id.userId = :userId
      AND p.isDeleted = false
      AND p.isMenus = true
      AND (
        p.permissionParent IS NULL
        OR p.permissionParent IN (
          SELECT p2.id
          FROM PermissionRole pr2
          JOIN Permission p2 ON pr2.id.permissionId = p2.id
          JOIN UserRoles ru2 ON ru2.id.roleId = pr2.id.roleId
          WHERE ru2.id.userId = :userId
            AND p2.isDeleted = false
            AND p2.isMenus = true
            AND p2.permissionParent IS NULL
        )
      )
""")
	List<Permission> findPermissionsByUserID(@Param("userId") UUID userId);


	@Query("""
    SELECT p
    FROM PermissionRole pr
    JOIN Permission p ON pr.id.permissionId = p.id
    JOIN UserRoles ru ON ru.id.roleId = pr.id.roleId
    WHERE ru.id.userId = :userId
      AND p.isDeleted = false
      AND p.isMenus = true
      AND p.permissionCode = :permissionCode
""")
	List<Permission> findChildPermissionsByUserID(@Param("userId") UUID userId, @Param("permissionCode") String permissionCode);

	@Query("""
    SELECT DISTINCT p
    FROM PermissionRole pr
    JOIN Permission p ON pr.id.permissionId = p.id
    JOIN UserRoles ur ON ur.id.roleId = pr.id.roleId
    WHERE ur.id.userId = :userId
      AND p.isDeleted = false
      AND p.isMenus = true
      AND p.permissionParent IN (
          SELECT parent.id
          FROM Permission parent
          WHERE parent.permissionCode = :parentCode
            AND parent.isDeleted = false
      )
    ORDER BY p.position
""")
	List<Permission> findChildrenByParentCodeAndUserId(
			@Param("parentCode") String parentCode,
			@Param("userId") UUID userId
	);

	@Query("""
    SELECT DISTINCT p.permissionCode
    FROM PermissionRole pr
    JOIN Permission p ON pr.id.permissionId = p.id
    JOIN UserRoles ur ON ur.id.roleId = pr.id.roleId
    WHERE ur.id.userId = :userId
      AND p.isDeleted = false
      AND p.isMenus = false
      AND p.permissionParent IN (
          SELECT parent.id
          FROM Permission parent
          WHERE parent.permissionCode = :parentCode
            AND parent.isDeleted = false
      )
""")
	List<String> getPermissionsCurrent(
			@Param("parentCode") String parentCode,
			@Param("userId") UUID userId
	);


	@Modifying
	@Query("DELETE FROM PermissionRole pr WHERE pr.id.roleId = :roleId")
	void deleteByRoleId(@Param("roleId") UUID roleId);

    @Query("FROM Role r WHERE r.id = :id AND r.isDeleted = false")
    Role getByRoleId(Long id);

}
