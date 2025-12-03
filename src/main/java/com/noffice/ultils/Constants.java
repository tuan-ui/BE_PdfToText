package com.noffice.ultils;

public final class Constants {
	public interface roleStatus {
		Integer UNLOCK = 1; // Mo khoa
        Integer LOCK = 0; // Bi khoa        
    }
	
	public interface isActive {
		Integer INACTIVE = 0; // Xoa
		Integer ACTIVE = 1; // Hoat dong
	}

    public interface isDeleted {
        Boolean ACTIVE = false; // Xoa
        Boolean DELETED = true; // Hoat dong
    }
	
	public static interface upload{
		public String IMAGE_DIRECTORY = AppConfig.get("save_path");
	}
	
	public static interface STATUS{
		public Long ACTIVE= 1L;
		public Long LOCKED = 0L;
	}

	public interface OBJECT_TYPE {
		Integer ATTACHS_CONFIG = 1;
        Integer DOC_DOCUMENT = 2;
    }

	public interface message {
		String NO_TOKEN_INFO = "Không có token hoặc phiên đăng nhập hợp lệ";
		String NO_USER_INFO = "Thông tin người dùng không hợp lệ";
		String SUCCESS = "Thành công";
		String SYSTEM_ERROR = "Lỗi hệ thống";
		String SYSTEM_ERROR_2 = "Lỗi hệ thống: ";
		String USER_NAME_MUST_NOT_NULL = "Username không được để trống";
		String ADD_SUCCESS = "Thêm mới thành công";
		String UPDATE_SUCCESS ="Cập nhật thành công";
	}
}
