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
		public Long ACTIVE= 1L;//hoat dong
		public Long INACTIVE =-1l;//da xoa
		public Long LOCKED = 0L;//da khoa
	}

	public interface OBJECT_TYPE {
		Integer ATTACHS_CONFIG = 1;
        Integer DOC_DOCUMENT = 2;
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

	public interface TYPE_APPROVE {
		String PARALLEL = "parallel";//song song
		String SEQUENTIAL = "sequential";//tuan tu
	}
}
