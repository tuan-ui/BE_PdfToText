package com.noffice.repository;

import com.noffice.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
    @Query("""
			FROM TaskType tt
			WHERE tt.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:taskTypeCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:taskTypeCode), '%')
					)
			AND (COALESCE(:taskTypeName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeName AS string)))
			LIKE CONCAT('%', LOWER(:taskTypeName), '%')
					)
			AND (COALESCE(:taskTypeDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(tt.taskTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:taskTypeDescription), '%')
					)
			AND tt.partnerId = :partnerId
			ORDER BY tt.createAt DESC
			""")
    Page<TaskType> getTaskTypeWithPagination(
            @Param("searchString") String searchString,
            @Param("taskTypeCode") String taskTypeCode,
            @Param("taskTypeName") String taskTypeName,
            @Param("taskTypeDescription") String taskTypeDescription,
            @Param("partnerId") UUID partnerId,
            Pageable pageable
    );

    @Query(value = "FROM TaskType tt " +
            "WHERE(:taskTypeCode IS NULL OR LOWER(tt.taskTypeCode) = LOWER(:taskTypeCode)) " +
            "AND tt.isDeleted = false AND tt.partnerId = :partnerId ")
    TaskType findByCode(@Param("taskTypeCode") String taskTypeCode, @Param("partnerId") UUID partnerId);

    @Query(value = "FROM TaskType tt " +
            "WHERE tt.taskTypeCode = :taskTypeCode " +
            "AND tt.isDeleted = false ")
    TaskType findByTaskTypeCode(@Param("taskTypeCode") String taskTypeCode);

    @Query("FROM TaskType tt WHERE tt.partnerId = :partnerId  AND tt.isDeleted = false AND tt.isActive = true")
    List<TaskType> getAllTaskType(UUID partnerId);

    @Query(value = "FROM TaskType tt " +
            "WHERE tt.id = :id")
    TaskType findByTaskTypeIdIncludeDeleted(@Param("id") UUID id);
}
