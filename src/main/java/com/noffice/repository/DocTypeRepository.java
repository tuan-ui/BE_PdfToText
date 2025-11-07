package com.noffice.repository;

import com.noffice.entity.DocType;
import com.noffice.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocTypeRepository extends JpaRepository<DocType, Long> {

	@Query("""
			FROM DocType d
			WHERE d.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:DocTypeCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:DocTypeCode), '%')
					)
			AND (COALESCE(:DocTypeName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeName AS string)))
			LIKE CONCAT('%', LOWER(:DocTypeName), '%')
					)
			AND (COALESCE(:DocTypeDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.docTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:DocTypeDescription), '%')
					)
			AND d.partnerId = :partnerId
			ORDER BY d.createAt DESC
			""")
	Page<DocType> getDocTypeWithPagination(
			@Param("searchString") String searchString,
			@Param("DocTypeCode") String DocTypeCode,
			@Param("DocTypeName") String DocTypeName,
			@Param("DocTypeDescription") String DocTypeDescription,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query(value = "FROM DocType d " +
			"WHERE(:DocTypeCode IS NULL OR LOWER(d.docTypeCode) = LOWER(:DocTypeCode)) " +
			"AND d.isDeleted = false AND d.partnerId = :partnerId ")
	DocType findByCode(@Param("DocTypeCode") String DocTypeCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM DocType d " +
			"WHERE d.docTypeCode = :DocTypeCode " +
			"AND d.isDeleted = false ")
	DocType findByDocTypeCode(@Param("DocTypeCode") String DocTypeCode);

	@Query("FROM DocType d WHERE d.partnerId = :partnerId  AND d.isDeleted = false AND d.isActive = true")
	List<DocType> getAllDocType(UUID partnerId);

	Optional<DocType> findById(UUID id);

	@Query(value = "FROM DocType d " +
			"WHERE d.id = :id")
	DocType findByDocTypeIdIncludeDeleted(@Param("id") UUID id);
}