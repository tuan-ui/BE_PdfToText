package com.noffice.repository;

import com.noffice.dto.DocDocumentDTO;
import com.noffice.entity.DocDocument;
import com.noffice.entity.DocType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {

	@Query("""
    SELECT new com.noffice.dto.DocDocumentDTO(
        d.id, d.documentTitle, d.deptName,d.purpose,d.formData, dt.id,d.docTemplateId, dt.docTypeName,d.createAt,d.updateAt,fs.id
    )
    FROM DocDocument d
    LEFT JOIN DocType dt ON dt.id = d.docTypeId AND dt.isDeleted = false
    LEFT JOIN DocumentTemplate dtemp ON d.docTemplateId = dtemp.id and dtemp.isDeleted = false
    LEFT JOIN FormSchema fs on fs.docTemplateId=dtemp.id and fs.isDeleted=false
    WHERE d.isDeleted = false
      AND (:searchString IS NULL OR :searchString = ''
        OR LOWER(FUNCTION('convert_to_unsign', d.documentTitle)) LIKE CONCAT('%', LOWER(:searchString), '%')
        OR LOWER(FUNCTION('convert_to_unsign', d.deptName)) LIKE CONCAT('%', LOWER(:searchString), '%'))
      AND (:documentTitle IS NULL OR :documentTitle = ''
        OR LOWER(FUNCTION('convert_to_unsign', d.documentTitle)) LIKE CONCAT('%', LOWER(:documentTitle), '%'))
      AND (:deptName IS NULL OR :deptName = ''
        OR LOWER(FUNCTION('convert_to_unsign', d.deptName)) LIKE CONCAT('%', LOWER(:deptName), '%'))
      AND d.partnerId = :partnerId
    ORDER BY d.createAt DESC
""")
	Page<DocDocumentDTO> getDocWithPagination(
			@Param("searchString") String searchString,
			@Param("documentTitle") String documentTitle,
			@Param("deptName") String deptName,
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

	@Query(value = "FROM DocDocument d " +
			"WHERE d.id = :id")
	DocDocument findByDocumentId(@Param("id") UUID id);
}