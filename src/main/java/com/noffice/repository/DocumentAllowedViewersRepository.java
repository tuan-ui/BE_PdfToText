package com.noffice.repository;

import com.noffice.entity.DocumentAllowedViewers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface DocumentAllowedViewersRepository extends JpaRepository<DocumentAllowedViewers, UUID> {
    List<DocumentAllowedViewers> findByDocumentId(UUID documentId);
    void deleteByDocumentIdAndViewerId(UUID documentId, UUID viewerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentAllowedViewers v WHERE v.documentId = :documentId")
    void deleteAllByDocumentId(UUID documentId);
}