package com.noffice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO chứa thông tin tìm kiếm vai trò")
public class RoleSearchDTO {
	@Schema(description = "Trường tìm kiếm nhanh theo: tên, mã, mô tả", example = "ADMIN")
	private String searchString;
	
	@Schema(description = "Tên vai trò cần tìm kiếm", example = "ADMIN")
	private String roleName;
	
	@Schema(description = "Mã vai trò cần tìm kiếm", example = "ROLE_ADMIN")
	private String roleCode;

	@Schema(description = "Mô tả vai trò", example = "Quản trị viên hệ thống")
	private String roleDescription;

	@Schema(description = "Id của đối tác")
	private Long partnerId = -1L;

	@Schema(description = "Trạng thái vai trò (1: Unlock, 0: Lock)")
	private Boolean status;

	@Schema(description = "Trang hiện tại của kết quả phân trang", example = "0")
	private Integer page = 0;

	@Schema(description = "Số lượng phần tử trên mỗi trang", example = "10")
	private Integer size = 10;

}
