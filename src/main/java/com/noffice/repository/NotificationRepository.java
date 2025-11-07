package com.noffice.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.noffice.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Query(value="SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.timeStamp DESC")
	Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);
	
	@Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND isRead = 0")
	Integer countByRecipientId(@Param("recipientId") Long recipientId);

}
