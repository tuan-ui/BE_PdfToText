package com.noffice.repository;

import com.noffice.entity.Attachs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachRepository extends JpaRepository<Attachs, UUID> {
    @Query("from Attachs a where a.isDeleted=false  and a.objectId=:objectId and a.objectType=:objectType")
    List<Attachs> getListAttachs(@Param("objectId") UUID objectId,@Param("objectType") Integer objectType);

    @Query("from Attachs a where a.isDeleted=false  and a.id=:attachId")
    Optional<Attachs> findById(@Param("attachId") String attachId);
}
