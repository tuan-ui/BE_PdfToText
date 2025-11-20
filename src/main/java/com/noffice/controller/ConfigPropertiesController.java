package com.noffice.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noffice.dto.ConfigPropertiesDTO;
import com.noffice.dto.ConfigPropertiesResponseDTO;
import com.noffice.entity.ConfigProperties;
import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.ConfigPropertiesService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/configProperties")
@Tag(name = "ConfigPropertiesController", description = "Cấu hình hệ thống")
public class ConfigPropertiesController {
	private final ConfigPropertiesService configPropertiesService;
	@PostMapping("/search")
	public ResponseEntity<ResponseAPI> searchRoles(@RequestBody @Parameter(description = "Thông tin tìm kiếm cấu hình") ConfigPropertiesDTO request) {
	    try {
	    	
	    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			ConfigPropertiesResponseDTO result=new ConfigPropertiesResponseDTO();
			Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());
			Page<ConfigProperties> page = configPropertiesService.searchConfig(
				    request.getSearchString(),
				    request.getKey(),
				    request.getTitle(),
				    request.getValue(),
				    request.getDescription(),
				    pageable
				);
			result.setRoles(page.getContent());
			result.setTotal(page.getTotalElements());
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(result, "Thành công", 200));
	    } catch (Exception e) {
	    	System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
	    }
	}
	
    @PostMapping("/createOrUpdate")
	public ResponseEntity<ResponseAPI> save(@RequestBody @Valid @Parameter(description = "Dữ liệu cấu hình cần thêm") ConfigPropertiesDTO configPropertiesDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			ConfigProperties result= configPropertiesService.createOrUpdate(configPropertiesDTO);
			if(result == null)
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thất bại !", 400));
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<ResponseAPI> delete(
	        @RequestParam(value = "id", required = true) String id,
	        HttpServletRequest request) {
	    try {
	    	configPropertiesService.delete(id);
	        return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
	    } catch (Exception e) {
	        System.out.println("Error : " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
	    }
	}
	
}
