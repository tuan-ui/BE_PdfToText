package com.noffice.repository;

import com.noffice.entity.DocumentFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentFileRepository extends JpaRepository<DocumentFiles, UUID> {

    @Query("SELECT m FROM DocumentFiles m WHERE m.attachName = :attachName")
    DocumentFiles findByAttachName(String attachName);


    @Query("SELECT m FROM DocumentFiles m WHERE m.originalFileId = :fileId AND m.isTemp = :isTemp")
    List<DocumentFiles> findByOriginalFileIdAndIsTemp(UUID fileId, boolean isTemp);

}