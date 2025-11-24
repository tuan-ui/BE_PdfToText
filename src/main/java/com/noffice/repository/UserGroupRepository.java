package com.noffice.repository;

import com.noffice.entity.Partners;
import com.noffice.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    @Query(
            value = "SELECT * FROM user_group ug " +
                    "WHERE ug.partner_id = :partnerId " +
                    "AND (LOWER(unaccent(ug.group_name)) LIKE LOWER(CONCAT('%', unaccent(:searchString), '%')) " +
                    "OR LOWER(unaccent(ug.group_code)) LIKE LOWER(CONCAT('%', unaccent(:searchString), '%'))) " +
                    "AND (:groupCode = '' OR LOWER(ug.group_code) LIKE LOWER(CONCAT('%', :groupCode, '%'))) " +
                    "AND (:groupName = '' OR LOWER(ug.group_name) LIKE LOWER(CONCAT('%', :groupName, '%'))) " +
                    "AND (:status IS NULL OR ug.is_active = :status) " +
                    "AND ug.is_deleted = false " +
                    "ORDER BY ug.create_at DESC",
            countQuery = "SELECT COUNT(*) FROM user_group ug " +
                    "WHERE ug.partner_id = :partnerId " +
                    "AND (LOWER(unaccent(ug.group_name)) LIKE LOWER(CONCAT('%', unaccent(:searchString), '%')) " +
                    "OR LOWER(unaccent(ug.group_code)) LIKE LOWER(CONCAT('%', unaccent(:searchString), '%'))) " +
                    "AND (:groupCode = '' OR LOWER(ug.group_code) LIKE LOWER(CONCAT('%', :groupCode, '%'))) " +
                    "AND (:groupName = '' OR LOWER(ug.group_name) LIKE LOWER(CONCAT('%', :groupName, '%'))) " +
                    "AND (:status IS NULL OR ug.is_active = :status) " +
                    "AND ug.is_deleted = false",
            nativeQuery = true
    )
    Page<UserGroup> searchUserGroup(
            @Param("partnerId") UUID partnerId,
            @Param("searchString") String searchString,
            @Param("groupCode") String groupCode,
            @Param("groupName") String groupName,
            @Param("status") Boolean status,
            Pageable pageable
    );

    @Query("SELECT ug FROM UserGroup ug WHERE ug.id = :id")
    UserGroup findByIdIncludeDeleted(UUID id);

    @Query(value = "FROM UserGroup p WHERE p.groupCode= :groupCode AND p.isDeleted = false")
    UserGroup getUserGroupByCode(@Param("groupCode") String groupCode);

    @Modifying
    @Query("DELETE FROM UserGroup d WHERE d.id = :id")
    void deleteUserGroupByUserGroupId(@Param("id") UUID id);
}
