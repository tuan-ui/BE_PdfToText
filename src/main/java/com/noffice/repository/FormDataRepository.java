package com.noffice.repository;

import com.noffice.entity.FormData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormDataRepository extends JpaRepository<FormData, Long> {
	@Query("SELECT m FROM FormData m WHERE m.formCode = :id")
	List<FormData> getFormSchema(String id);
}