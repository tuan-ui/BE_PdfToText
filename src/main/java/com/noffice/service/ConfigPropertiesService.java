package com.noffice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.noffice.dto.ConfigPropertiesDTO;
import com.noffice.entity.ConfigProperties;
import com.noffice.repository.ConfigRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class ConfigPropertiesService {
	
	@Autowired
	private ConfigRepository configRepository;
	
	//get by label
	public String getByLabel(String key) {
		return configRepository.findByKey(key).getValue();
	}
	
	//update by label
	public void updateByLabel(String label, String value) {
		configRepository.updateByLabel(label, value);
	}
	
	public Page<ConfigProperties> searchConfig(String searchAll, String key, String title, String value, String description, Pageable pageable) {
        return configRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchAll != null && !searchAll.isBlank()) {
                String s = "%" + searchAll.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("key")), s),
                    cb.like(cb.lower(root.get("title")), s),
                    cb.like(cb.lower(root.get("value")), s),
                    cb.like(cb.lower(root.get("description")), s)
                ));
            }

            if (key != null && !key.isBlank())
                predicates.add(cb.like(cb.lower(root.get("key")), "%" + key.toLowerCase() + "%"));
            if (title != null && !title.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            if (value != null && !value.isBlank())
                predicates.add(cb.like(cb.lower(root.get("value")), "%" + value.toLowerCase() + "%"));
            if (description != null && !description.isBlank())
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
	
	@Transactional
	public ConfigProperties createOrUpdate(@Valid ConfigPropertiesDTO request) {
		ConfigProperties configProperties=new ConfigProperties();
		configProperties.setId(request.getId());
		configProperties.setTitle(request.getTitle());
		configProperties.setKey(request.getKey());
		configProperties.setDescription(request.getDescription());
		configProperties.setValue(request.getValue());
		return configRepository.save(configProperties);
	}

	@Transactional
	public void delete(String id) {
		Optional<ConfigProperties> configProperties=configRepository.findById(Long.parseLong(id));
		if (configProperties.isPresent()) {
		    ConfigProperties entity = configProperties.get();
		    configRepository.delete(entity);
		}
	}
	
}
