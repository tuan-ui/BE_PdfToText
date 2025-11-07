package com.noffice.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.noffice.entity.EmailConfig;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Integer> {
	

}
