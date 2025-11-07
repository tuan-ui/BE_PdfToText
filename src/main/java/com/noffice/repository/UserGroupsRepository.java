package com.noffice.repository;

import com.noffice.entity.UserGroups;
import com.noffice.entity.UserGroupsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserGroupsRepository extends JpaRepository<UserGroups, UserGroupsId> {
    @Modifying
    @Query("DELETE FROM UserGroups ug WHERE ug.id.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);

    @Query("Select ug.id.userId FROM UserGroups ug WHERE ug.id.groupId = :groupId")
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT COUNT(ug) > 0 FROM UserGroups ug WHERE ug.id.userId = :userId")
    Boolean existsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ug) > 0 FROM UserGroups ug WHERE ug.id.groupId = :groupId")
    boolean existsUserByGroupId(@Param("groupId") UUID groupId);
}
