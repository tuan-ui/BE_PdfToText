package com.noffice.repository;

import com.noffice.entity.DocumentFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentFileRepository extends JpaRepository<DocumentFiles, UUID> {

}