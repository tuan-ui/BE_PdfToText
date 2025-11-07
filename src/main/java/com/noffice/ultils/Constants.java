package com.noffice.ultils;

public final class Constants {
	public interface ROLE_STATUS {
		Integer UNLOCK = 1; // Mo khoa
        Integer LOCK = 0; // Bi khoa        
    }
	
	public interface IS_ACTIVE {
		Integer INACTIVE = 0; // Xoa
		Integer ACTIVE = 1; // Hoat dong
	}

    public interface IS_DELETED {
        Boolean ACTIVE = false; // Xoa
        Boolean DELETED = true; // Hoat dong
    }
	
	public static interface PARTNER_STATUS {
		public String ACTIVE ="ACTIVE";//hoat dong
		public String EXPIRE ="EXPIRE";//het han
		public String LOCK="LOCK";//khoa
	}
	
	public static interface UPLOAD{
		public String IMAGE_DIRECTORY = AppConfig.get("save_path");
	}
	
	public static interface STATUS{
		public Long ACTIVE= 1L;//hoat dong
		public Long INACTIVE =-1l;//da xoa
		public Long LOCKED = 0L;//da khoa
	}

	public interface OBJECT_TYPE {
		Integer ATTACHS_CONFIG = 1;
		Integer MORTGAGE_CONFIG=2;
		Integer CUSTOMER_CONFIG=5;
		Integer PROPERTY_CONFIG=6;
		Integer TASK_CONFIG=7;
        Integer TASK_PROGRESS_CONFIG=8;
        Integer CREDIT_CONFIG=9;
        Integer CCCD_USER_CONFIG = 10;
    }
	
    public static interface FOLDER_OBJECT {
        public String CUSTOMER = "CUSTOMER";
		public String UPLOADEVALFILE = "UPLOADEVALFILE";
        public String AUTOGEN = "AUTOGEN";
		public String PROPERTY = "PROPERTY";
		public String TASK_FILE = "TASK_FILE";
    }
    public static interface CACLOUD {
        public String CA_CLOUD_CLIENT_ID = "CA_CLOUD_CLIENT_ID";
        public String CA_CLOUD_CLIENT_SECRET = "CA_CLOUD_CLIENT_SECRET";
        public String CA_CLOUD_PROFILE_ID = "CA_CLOUD_PROFILE_ID";
    }
    
    public static interface NOTIFICATION_TYPE {
        public String ADD_MORTGAGECONTRACT = "ADD_MORTGAGECONTRACT"; // Thiết lập hồ sơ thế chấp (người trong luồng được nhận thông báo)
        public String ADD_CUSTOMER = "ADD_CUSTOMER"; // Thiết lập hồ sơ khách hàng (phần quyền trong phần quyền vai trò nào nhận được thông báo)
        
        // HỢP ĐỒNG TÍN DỤNG
        public String CREATE_CREDITCONTRACT = "CREATE_CREDITCONTRACT"; // Thiết lập hợp đồng tín dụng: Người trong luồng
        public String EXPIRE_CREDITCONTRACT = "EXPIRE_CREDITCONTRACT"; // Hợp đồng tín dụng sắp hết hạn: Người trong luồng
        public String OVERDUE_CREDITCONTRACT = "OVERDUE_CREDITCONTRACT"; // Hợp đồng tín dụng quá hạn: Người trong luồng
        public String APPROVE_CREDITCONTRACT = "APPROVE_CREDITCONTRACT"; // Hợp đồng tín dụng đã được phê duyệt: Người trong luồng
        public String REJECT_CREDITCONTRACT = "REJECT_CREDITCONTRACT"; // Hợp đồng tín dụng bị trả lại: Toàn bộ người trong luồng
        public String COMMENT_CREDITCONTRACT = "COMMENT_CREDITCONTRACT"; // Ý kiến xử lý: Toàn bộ người trong luồng và người có ý kiến đó
        public String UPDATE_CREDITCONTRACT = "UPDATE_CREDITCONTRACT"; // Sửa hợp đồng tín dụng

        // CÔNG VIỆC
        public String DELETE_TASK = "DELETE_TASK"; // Xóa công việc
        public String REGIS_TASK = "REGIS_TASK"; // Đăng ký công việc: Người được đăng ky
        public String ASSIGN_TASK = "ASSIGN_TASK"; // Giao việc: Người giao việc
        public String DENIED_TASK = "DENIED_TASK"; // Từ chối: người đăng ký công việc
        public String APPROVE_TASK = "APPROVE_TASK"; // Phê duyệt công việc: Người trong luồng và người đăng ký
        public String EXPIRE_TASK = "EXPIRE_TASK"; // Công việc hết hạn: Người giao việc và người nhận để biết
        public String SUBMIT_EVALUATION = "SUBMIT_EVALUATION"; // Công việc gửi đánh giá: Tất cả người liên quan đến công việc
        public String EVALUATED_TASK = "EVALUATED_TASK"; // Công việc được đánh giá: Tất cả người liên quan đến công việc
        public String REPROCESS_TASK = "REPROCESS_TASK"; // Công việc bị xử lý lại: Tất cả người liên quan
        public String COMMENT_TASK = "COMMENT_TASK"; // Ý kiến xử lý: Toàn bộ người trong luồng và người có ý kiến đó
        public String TRIAL_REGISTER = "TRIAL_REGISTER"; // Đăng ký dùng thử
        public String TRIAL_APPROVE = "TRIAL_APPROVE"; // Đăng ký dùng thử
        public String RECEIVE_TRIAL = "RECEIVE_TRIAL"; //Nhận được đăng ký dùng thử
        public String ACCOUNT_EMPLOYEE = "ACCOUNT_EMPLOYEE";
    }

    public static interface TASK_STATUS {
        Integer SUBMIT_EVALUATION = 1; // Gửi đánh giá
        Integer EVALUATED = 2; // Đã đánh giá
        Integer IN_PROGRESS = 3; // Đang xử lý
        Integer SUBMIT_REGISTRATION = 4; // Gửi đăng ký
        Integer REPROCESS = 5; // Xử lý lại
        Integer DENIED = 6; // Từ chối
    }

    public static interface PERSONAL_STATUS {
        Integer MAIN_PROCESSOR = 1; // Xử lý chính
        Integer COORDINATOR = 2; // Phối hợp xử lý
        Integer NOT_PROCESSING = 3; // Không xử lý
        Integer NOT_APPROVED = 4; // Chưa phê duyệt
        Integer NOT_EVALUATED = 5; // Chưa đánh giá
        Integer REJECTED = 6; // Từ chối
        Integer BEING_REJECTED = 7; // Bị từ chối
        Integer EVALUATED = 8; // Đã đánh giá
        Integer APPROVED = 9; // Đã phê duyệt
        Integer SUBMIT_REGISTRATION = 10; // Gửi đăng ký
        Integer COMPLETE = 11; // Gửi đăng ký
        Integer ASSIGNED = 12; // Đã giao việc


    }

    public interface OCR_TYPE {
        Integer PROCESS_PROPERTY = 1;
        Integer PROCESS_CCCD = 2;
        Integer EXTRACT_COLLATERAL = 3;
        Integer EXTRACT_CIC_INFO = 4;
        Integer EXTRACT_LOAN_PROJECT = 5;
    }
}
