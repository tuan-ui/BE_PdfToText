package com.noffice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.noffice.entity.Permission;

import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permission, Long> {
    @Query(value =
            "FROM Permission p " +
            "WHERE p.isDeleted = false "+
            "ORDER BY p.position ASC")
    List<Permission> findAllByIsDeletedFalseOrderByPositionAsc();
}
