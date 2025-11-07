package com.noffice.repository;

import com.noffice.entity.FormSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FormSchemaRepository extends JpaRepository<FormSchema, Long> {
	@Query("SELECT m FROM FormSchema m WHERE m.formCode = :id")
	FormSchema getFormSchema(String id);

	@Query(value = """
    SELECT f
    FROM FormSchema f
    WHERE f.isDeleted = false
      AND (COALESCE(:formName, '') = ''
           OR LOWER(FUNCTION('convert_to_unsign', CAST(f.formName AS string))) 
              LIKE CONCAT('%', LOWER(:formName), '%'))
      AND (COALESCE(:formCode, '') = '' 
           OR LOWER(FUNCTION('convert_to_unsign', CAST(f.formCode AS string))) 
              LIKE CONCAT('%', LOWER(:formCode), '%'))
      AND f.partnerId = :partnerId
      AND (:status IS NULL OR f.isActive = :status)
    ORDER BY f.createAt ASC
    """)
	Page<FormSchema> searchFormSchemas(
			@Param("formName") String formName,
			@Param("formCode") String formCode,
			@Param("status") Boolean status,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query("FROM FormSchema f WHERE f.id = :id AND f.isDeleted = false")
	FormSchema findByFormSchemaId(UUID id);
}