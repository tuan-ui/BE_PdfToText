package com.noffice.repository;

import com.noffice.entity.DocumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
			AND (COALESCE(:documentTemplateCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateCode AS string)))
			LIKE CONCAT('%', LOWER(:documentTemplateCode), '%')
					)
			AND (COALESCE(:documentTemplateName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateName AS string)))
			LIKE CONCAT('%', LOWER(:documentTemplateName), '%')
					)
			AND (COALESCE(:documentTemplateDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.documentTemplateDescription AS string)))
			LIKE CONCAT('%', LOWER(:documentTemplateDescription), '%')
					)
			AND d.partnerId = :partnerId
			ORDER BY d.createAt DESC
			""")
	Page<DocumentTemplate> getDocumentTemplateWithPagination(
			@Param("searchString") String searchString,
			@Param("documentTemplateCode") String documentTemplateCode,
			@Param("documentTemplateName") String documentTemplateName,
			@Param("documentTemplateDescription") String documentTemplateDescription,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE(:documentTemplateCode IS NULL OR LOWER(d.documentTemplateCode) = LOWER(:documentTemplateCode)) " +
			"AND d.isDeleted = false AND d.partnerId = :partnerId ")
	DocumentTemplate findByCode(@Param("documentTemplateCode") String documentTemplateCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.documentTemplateCode = :documentTemplateCode " +
			"AND d.isDeleted = false ")
	DocumentTemplate findByDocumentTemplateCode(@Param("documentTemplateCode") String documentTemplateCode);

	@Query("FROM DocumentTemplate d WHERE d.partnerId = :partnerId  AND d.isDeleted = false AND d.isActive = true")
	List<DocumentTemplate> getAllDocumentTemplate(UUID partnerId);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.id = :id AND d.isDeleted = false AND d.isActive = true")
	DocumentTemplate findById(@Param("id") UUID id);

	@Query(value = "FROM DocumentTemplate d " +
			"WHERE d.id = :id")
	DocumentTemplate findByDocumentTemplateIdIncludeDeleted(@Param("id") UUID id);


	@Modifying
	@Query("DELETE FROM DocumentTemplate d WHERE d.id = :id")
	void deleteDocumentTemplateByDocumentTemplateId(@Param("id") UUID id);
}