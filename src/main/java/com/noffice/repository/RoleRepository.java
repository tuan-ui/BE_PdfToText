package com.noffice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noffice.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.noffice.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	@Query("SELECT COUNT(r) > 0 FROM Role r WHERE LOWER(r.roleCode) = LOWER(:roleCode) AND r.isActive = true AND r.isDeleted = false AND r.partnerId = :partnerId")
	boolean existsByRoleCodeIgnoreCase(@Param("roleCode") String roleCode, @Param("partnerId") UUID partnerId);

	@Query("SELECT COUNT(r) > 0 FROM Role r WHERE LOWER(r.roleCode) = LOWER(:roleCode) AND r.isActive = true AND r.isDeleted = false AND r.id <> :roleId AND r.partnerId = :partnerId")
	boolean existsByRoleCodeIgnoreCaseNotId(@Param("roleCode") String roleCode, @Param("roleId") UUID roleId, @Param("partnerId") UUID partnerId);

	@Query("""
    SELECT new com.noffice.dto.RoleDTO(
        r.id,
        r.roleName,
        r.roleCode,
        r.roleDescription,
        r.version,
        r.partnerId,
        pn.partnerName,
        r.isActive,
        r.isDeleted,
        r.createAt,
        r.updateAt,
        r.createBy,
        r.updateBy
    )
    FROM Role r
    LEFT JOIN Partners pn ON r.partnerId = pn.id
    WHERE r.isDeleted = false
      AND (COALESCE(:searchString, '') = ''
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleName AS string))) 
              LIKE CONCAT('%', LOWER(:searchString), '%')
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleCode AS string))) 
              LIKE CONCAT('%', LOWER(:searchString), '%')
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleDescription AS string))) 
              LIKE CONCAT('%', LOWER(:searchString), '%')
          )
      AND (COALESCE(:roleName, '') = ''
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleName AS string))) 
              LIKE CONCAT('%', LOWER(:roleName), '%')
          )
      AND (COALESCE(:roleCode, '') = ''
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleCode AS string))) 
              LIKE CONCAT('%', LOWER(:roleCode), '%')
          )
      AND (COALESCE(:roleDescription, '') = ''
           OR LOWER(FUNCTION('convert_to_unsign', CAST(r.roleDescription AS string))) 
              LIKE CONCAT('%', LOWER(:roleDescription), '%')
          )
      AND r.partnerId = :partnerId
      AND (:status IS NULL OR r.isActive = :status)
    ORDER BY r.roleName ASC
    """)
	Page<RoleDTO> searchRoles(
			@Param("searchString") String searchString,
			@Param("roleName") String roleName,
			@Param("roleCode") String roleCode,
			@Param("roleDescription") String roleDescription,
			@Param("partnerId") UUID partnerId,
			@Param("status") Boolean status,
			Pageable pageable
	);


	@Query("SELECT r FROM Role r WHERE r.partnerId = :partnerId AND r.isActive = true AND r.isDeleted = false")
	List<Role> findByOnlyPartnerId(@Param("partnerId") UUID partnerId);

	@Query("FROM Role r WHERE r.id = :id AND r.isDeleted = false")
	Optional<Role> findByRoleId(UUID id);

	@Query("FROM Role r WHERE r.id = :id ")
	Role findByRoleIdIncluideDeleted(UUID id);

	@Modifying
	@Query("DELETE FROM Role d WHERE d.id = :id")
	void deleteRoleByRoleId(@Param("id") UUID id);
}
