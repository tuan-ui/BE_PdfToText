package com.noffice.repository;

import com.noffice.entity.DocumentAllowedEditors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface DocumentAllowedEditorsRepository extends JpaRepository<DocumentAllowedEditors, UUID> {
    List<DocumentAllowedEditors> findByDocumentId(UUID documentId);
    void deleteByDocumentIdAndEditorId(UUID documentId, UUID editorId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentAllowedEditors e WHERE e.documentId = :documentId")
    void deleteAllByDocumentId(UUID documentId);
}
