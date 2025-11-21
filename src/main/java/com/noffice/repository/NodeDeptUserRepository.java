package com.noffice.repository;

import com.noffice.dto.DocDocumentDTO;
import com.noffice.entity.DocDocument;
import com.noffice.entity.DocType;
import com.noffice.entity.NodeDeptUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeDeptUserRepository extends JpaRepository<NodeDeptUser, UUID> {

    @Query("from NodeDeptUser ndu where ndu.docId=:docId and ndu.isDeleted=false ")
    List<NodeDeptUser>getByDocId(@Param("docId") UUID docId);
}