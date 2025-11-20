package com.noffice.repository;

import com.noffice.entity.DocumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

	@Query("""
			FROM DocumentTemplate d
			WHERE d.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:DocumentTemplateCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateCode AS string)))
			LIKE CONCAT('%', LOWER(:DocumentTemplateCode), '%')
					)
			AND (COALESCE(:DocumentTemplateName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateName AS string)))
			LIKE CONCAT('%', LOWER(:DocumentTemplateName), '%')
					)
			AND (COALESCE(:DocumentTemplateDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateDescription AS string)))
			LIKE CONCAT('%', LOWER(:DocumentTemplateDescription), '%')
					)
			AND d.partnerId = :partnerId
			ORDER BY d.createAt DESC
			""")
	Page<DocumentTemplate> getDocumentTemplateWithPagination(
			@Param("searchString") String searchString,
			@Param("DocumentTemplateCode") String DocumentTemplateCode,
			@Param("DocumentTemplateName") String DocumentTemplateName,
			@Param("DocumentTemplateDescription") String DocumentTemplateDescription,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE(:DocumentTemplateCode IS NULL OR LOWER(d.documentTemplateCode) = LOWER(:DocumentTemplateCode)) " +
			"AND d.isDeleted = false AND d.partnerId = :partnerId ")
	DocumentTemplate findByCode(@Param("DocumentTemplateCode") String DocumentTemplateCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.documentTemplateCode = :DocumentTemplateCode " +
			"AND d.isDeleted = false ")
	DocumentTemplate findByDocumentTemplateCode(@Param("DocumentTemplateCode") String DocumentTemplateCode);

	@Query("FROM DocumentTemplate d WHERE d.partnerId = :partnerId  AND d.isDeleted = false AND d.isActive = true")
	List<DocumentTemplate> getAllDocumentTemplate(UUID partnerId);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.id = :id AND d.isDeleted = false AND d.isActive = true")
	DocumentTemplate findById(@Param("id") UUID id);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.id = :id")
	DocumentTemplate findByDocumentTemplateIdIncludeDeleted(@Param("id") UUID id);
}