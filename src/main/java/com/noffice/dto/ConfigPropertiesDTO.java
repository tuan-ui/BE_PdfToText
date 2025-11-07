package com.noffice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO chứa thông tin tìm kiếm vai trò")
public class ConfigPropertiesDTO {
	@Schema(description = "Trường tìm kiếm nhanh theo: tên, mã, mô tả", example = "ADMIN")
	private String searchString;
	@Schema(description = "Trang hiện tại của kết quả phân trang", example = "0")
	private Integer page = 0;

	@Schema(description = "Số lượng phần tử trên mỗi trang", example = "10")
	private Integer size = 10;
	@Schema(description = "Đăng nhập trang lần đàu", example = "10")
	private Boolean isFirstLog = false;
	
	private String title;
	private String key;
	private String value;
	private String description;
	private Long id;
	
}
