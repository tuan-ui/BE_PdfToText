package com.noffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.noffice.entity.ConfigProperties;

public interface ConfigRepository extends JpaRepository<ConfigProperties, Long>,JpaSpecificationExecutor<ConfigProperties> {
	
	ConfigProperties findByKey(String KEY);
	
	//update by label
	@Query(value = "update config_properties set VALUE = ?2 where KEY = ?1", nativeQuery = true)
	@Modifying
	void updateByLabel(String KEY, String VALUE);

}
