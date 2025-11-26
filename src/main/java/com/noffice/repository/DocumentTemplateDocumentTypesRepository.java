package com.noffice.repository;

import com.noffice.entity.DocTemplateDocTypes;
import com.noffice.entity.DocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentTemplateDocumentTypesRepository extends JpaRepository<DocTemplateDocTypes, UUID> {
    @Modifying
    @Query("DELETE FROM DocTemplateDocTypes pr WHERE pr.documentTemplateId = :documentTemplateId")
    void deleteByDocumentTemplateId(@Param("documentTemplateId") UUID documentTemplateId);
    @Query("Select pr.documentTypeId FROM DocTemplateDocTypes pr WHERE pr.documentTemplateId = :documentTemplateId")
    List<UUID> getDocumentTypeIdByDocumentTemplateId(@Param("documentTemplateId") UUID documentTemplateId);
    @Query("SELECT COUNT(pr) > 0 FROM DocTemplateDocTypes pr WHERE pr.documentTypeId = :documentTypeId")
    boolean existsDocumentTemplateByDocumentTypeId(@Param("documentTypeId") UUID documentTypeId);
    @Query("SELECT COUNT(pr) > 0 FROM DocTemplateDocTypes pr WHERE pr.documentTemplateId = :documentTemplateId")
    boolean existsDocumentTemplateBydocumentTemplateId(@Param("documentTemplateId") UUID documentTemplateId);

    @Query("Select dt FROM DocTemplateDocTypes pr Join DocType dt on pr.documentTypeId = dt.id WHERE pr.documentTemplateId = :documentTemplateId")
    List<DocType> getDocumentTypesByDocumentTemplateId(@Param("documentTemplateId") UUID documentTemplateId);
}
