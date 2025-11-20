package com.noffice.enumType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FunctionType {
    LOGIN("log.action.auth.login"),
//    VIEW_USER_INFO("Xem thông tin người dùng"),
    CHANGE_PASSWORD("log.action.auth.changePassword"),
//    UPDATE_USER_INFO("Đổi thông tin người dùng"),
//    LOGIN_HISTORY("Lịch sử đăng nhập"),
//    CHANGE_ROLE("Đổi vai trò"),
//    USER_GUIDE("Hướng dẫn sử dụng"),
    LOGOUT("log.action.auth.logout"),
//
//    STATISTICS_PROCESSING("Thống kê số liệu xử lý hồ sơ"),
//    STATISTICS_TASKS("Thống kê số liệu công việc"),
//

    VIEW_DETAIL_USER("log.action.user.view"),
    CREATE_USER("log.action.user.create"),
    EDIT_USER("log.action.user.edit"),
    DELETE_USER("log.action.user.delete"),
    LOCK_USER("log.action.user.lock"),
    UNLOCK_USER("log.action.user.unlock"),

    VIEW_DETAIL_DOCTYPE("log.action.docType.view"),
    CREATE_DOCTYPE("log.action.docType.create"),
    EDIT_DOCTYPE("log.action.docType.edit"),
    DELETE_DOCTYPE("log.action.docType.delete"),
    LOCK_DOCTYPE("log.action.docType.lock"),
    UNLOCK_DOCTYPE("log.action.docType.unlock"),

    VIEW_DETAIL_CONTRACTTYPE("log.action.contractType.view"),
    CREATE_CONTRACTTYPE("log.action.contractType.create"),
    EDIT_CONTRACTTYPE("log.action.contractType.edit"),
    DELETE_CONTRACTTYPE("log.action.contractType.delete"),
    LOCK_CONTRACTTYPE("log.action.contractType.lock"),
    UNLOCK_CONTRACTTYPE("log.action.contractType.unlock"),

    VIEW_DETAIL_HOLIDAYTYPE("log.action.holidayType.view"),
    CREATE_HOLIDAYTYPE("log.action.holidayType.create"),
    EDIT_HOLIDAYTYPE("log.action.holidayType.edit"),
    DELETE_HOLIDAYTYPE("log.action.holidayType.delete"),
    LOCK_HOLIDAYTYPE("log.action.holidayType.lock"),
    UNLOCK_HOLIDAYTYPE("log.action.holidayType.unlock"),

    VIEW_DETAIL_TASKTYPE("log.action.taskType.view"),
    CREATE_TASKTYPE("log.action.taskType.create"),
    EDIT_TASKTYPE("log.action.taskType.edit"),
    DELETE_TASKTYPE("log.action.taskType.delete"),
    LOCK_TASKTYPE("log.action.taskType.lock"),
    UNLOCK_TASKTYPE("log.action.taskType.unlock"),

    VIEW_DETAIL_DOMAIN("log.action.domain.view"),
    CREATE_DOMAIN("log.action.domain.create"),
    EDIT_DOMAIN("log.action.domain.edit"),
    DELETE_DOMAIN("log.action.domain.delete"),
    LOCK_DOMAIN("log.action.domain.lock"),
    UNLOCK_DOMAIN("log.action.domain.unlock"),

    VIEW_DETAIL_ROLE("log.action.role.view"),
    CREATE_ROLE("log.action.role.create"),
    EDIT_ROLE("log.action.role.edit"),
    DELETE_ROLE("log.action.role.delete"),
    LOCK_ROLE("log.action.role.lock"),
    UNLOCK_ROLE("log.action.role.unlock"),
//
//    USER_LOG_LIST("Danh sách log người dùng"),
//    SEARCH_USER_LOG("Tìm kiếm, lọc log người dùng"),
//
//    CATEGORY_TYPE_LIST("Danh sách loại danh mục"),
//    CREATE_CATEGORY_TYPE("Thêm mới loại danh mục cho loại danh mục"),
//    SEARCH_CATEGORY_TYPE("Tìm kiếm loại danh mục"),
//    VIEW_CATEGORY_TYPE("Xem thông tin danh mục động"),
//    DELETE_CATEGORY_TYPE("Xóa thông tin loại danh mục"),
//    UPDATE_CATEGORY_TYPE("Sửa thông tin loại danh mục"),
//    LOCK_UNLOCK_CATEGORY_TYPE("Khóa/mở khóa loại danh mục"),
//    CREATE_CATEGORY("Thêm mới danh mục cho loại danh mục"),
//    SEARCH_CATEGORY("Tìm kiếm danh mục"),
//    UPDATE_CATEGORY("Sửa thông tin danh mục"),
//    DELETE_CATEGORY("Xóa thông tin danh mục"),
//    LOCK_UNLOCK_CATEGORY("Khóa/mở khóa danh mục"),
//
//    ATTACH_CONFIG_LIST("Danh sách cấu hình sinh file"),
//    CREATE_ATTACH_CONFIG("Thêm mới cấu hình sinh file"),
//    VIEW_ATTACH_CONFIG("Xem thông cấu hình sinh file"),
//    UPDATE_ATTACH_CONFIG("Cập nhật cấu hình sinh file"),
//    LOCK_UNLOCK_ATTACH_CONFIG("Khóa/mở cấu hình sinh file"),
//
//    BENEFICIARY_LIST("Danh sách người thụ hưởng"),
//    SEARCH_BENEFICIARY("Tìm kiếm, lọc thông tin người thụ hưởng"),
//    CREATE_BENEFICIARY("Thêm mới người người thụ hưởng"),
//    VIEW_BENEFICIARY("Xem thông tin người thụ hưởng"),
//    UPDATE_BENEFICIARY("Cập nhật thông tin người thụ hưởng"),
//    LOCK_UNLOCK_BENEFICIARY("Khóa/mở người thụ hưởng"),
//    LOCK_UNLOCK_ROLE_DEP("Khóa/mở vai trò - phòng ban người dùng"),
//

    VIEW_DETAIL_PARTNER("log.action.partner.view"),
    CREATE_PARTNER("log.action.partner.create"),
    UPDATE_PARTNER("log.action.partner.edit"),
    LOCK_PARTNER("log.action.partner.lock"),
    UNLOCK_PARTNER("log.action.partner.unlock"),
    DELETE_PARTNER("log.action.partner.delete"),

    VIEW_DETAIL_DEPARTMENT("log.action.department.view"),
    CREATE_DEPARTMENT("log.action.department.create"),
    EDIT_DEPARTMENT("log.action.department.edit"),
    DELETE_DEPARTMENT("log.action.department.delete"),
    LOCK_DEPARTMENT("log.action.department.lock"),
    UNLOCK_DEPARTMENT("log.action.department.unlock"),

    VIEW_DETAIL_USER_GROUP("log.action.userGroup.view"),
    CREATE_USER_GROUP("log.action.userGroup.create"),
    EDIT_USER_GROUP("log.action.userGroup.edit"),
    LOCK_USER_GROUP("log.action.userGroup.lock"),
    UNLOCK_USER_GROUP("log.action.userGroup.unlock"),
    DELETE_USER_GROUP("log.action.userGroup.delete"),

    VIEW_DETAIL_DOC_TEMPLATE("log.action.docTemplate.view"),
    CREATE_DOC_TEMPLATE("log.action.docTemplate.create"),
    EDIT_DOC_TEMPLATE("log.action.docTemplate.edit"),
    DELETE_DOC_TEMPLATE("log.action.docTemplate.delete"),
    LOCK_DOC_TEMPLATE("log.action.docTemplate.lock"),
    UNLOCK_DOC_TEMPLATE("log.action.docTemplate.unlock"),

//    BUSINESS_CONFIG_LIST("Danh sách cấu hình nghiệp vụ"),
//    CREATE_BUSINESS_GOAL("Thêm mục đích kinh doanh"),
//    UPDATE_BUSINESS_GOAL("Sửa mục đích kinh doanh"),
//    DELETE_BUSINESS_GOAL("Xóa mục đích kinh doanh"),
//
//    CREATE_APPRAISAL_FACTOR("Thêm yếu tố thẩm định"),
//    UPDATE_APPRAISAL_FACTOR("Sửa yếu tố thẩm định"),
//    DELETE_APPRAISAL_FACTOR("Xóa yếu tố thẩm định"),
//
//    CREATE_SURVEY_CONTENT("Thêm nội dung khảo sát gợi ý"),
//    UPDATE_SURVEY_CONTENT("Sửa nội dung khảo sát gợi ý"),
//    DELETE_SURVEY_CONTENT("Xóa nội dung khảo sát gợi ý"),
//
//    UPLOAD_VALUATION_FILE("Upload file định giá"),
//    VALUATION_FILE_HISTORY("Danh sách lịch sử file định giá"),
//    DOWNLOAD_VALUATION_FILE("Tải file định giá"),
//    DOWNLOAD_SAMPLE_FILE("Tải file mẫu"),
//
//    CUSTOMER_LIST("Danh sách khách hàng"),
//    SEARCH_CUSTOMER("Tìm kiếm, lọc thông tin khách hàng"),
//    CREATE_CUSTOMER("Thêm mới khách hàng"),
//    VIEW_CUSTOMER("Xem thông tin khách hàng"),
//    UPDATE_CUSTOMER("Cập nhật thông tin khách hàng"),
//    LOCK_UNLOCK_CUSTOMER("Khóa, mở khóa khách hàng"),
//    EXPORT_FILE("Xuất file"),
//
//    MORTGAGE_CONTRACT_LIST("Danh sách hợp đồng thế chấp"),
//    SEARCH_MORTGAGE_CONTRACT("Tìm kiếm hợp đồng thế chấp"),
//    CREATE_MORTGAGE_CONTRACT("Thêm hợp đồng thế chấp"),
//    EXPORT_MORTGAGE_FILE("Xuất file"),
//    ADD_ASSET_TO_CONTRACT("Thêm tài sản trong hợp đồng thế chấp"),
//    UPDATE_MORTGAGE_CONTRACT("Sửa thông tin hợp đồng thế chấp"),
//    INHERIT_FROM_OTHER_CONTRACT("Thừa hưởng từ hợp đồng thế chấp khác"),
//    VIEW_MORTGAGE_CONTRACT("Xem chi tiết hợp đồng thế chấp"),
//
//    CREDIT_CONTRACT_LIST("Danh sách hợp đồng tín dụng"),
//    SEARCH_CREDIT_CONTRACT("Tìm kiếm hợp đồng tín dụng"),
//    VIEW_CREDIT_CONTRACT("Xem danh sách hợp đồng tín dụng"),
//    VIEW_DETAIL_CREDIT_CONTRACT("Xem chi tiết hợp đồng tín dụng"),
//    PROCESS_CREDIT_CONTRACT("Xử lý hợp đồng tín dụng"),
//    VIEW_ATTACHMENTS("Xem file đính kèm"),
//    DOWNLOAD_ATTACHMENTS("Tải file đính kèm"),
//    FOLLOW("Theo dõi"),
//    REJECT_REVIEW_APPLICATION("Từ chối hồ sơ, đánh giá hồ sơ"),
//    ADD_CREDIT_CONTRACT("Thêm hợp đồng tín dụng"),
//    UPDATE_CREDIT_CONTRACT("Sửa hợp đồng tín dụng"),
//    INHERIT_CREDIT_CONTRACT("Thừa hưởng từ hợp đồng tín dụng khác"),
//
//    TASK("Công việc"),
//    TASK_LIST("Danh sách công việc"),
//    SEARCH_TASK("Tìm kiếm/lọc thông tin công việc"),
//    VIEW_TASK("Xem chi tiết công việc"),
//    UPDATE_PROCESS("Cập nhật tiến độ"),
//    UPDATE_TASK("Cập nhật công việc"),
//    CREATE_TASK("Tạo công việc"),
//    REGISTER_TASK("Đăng ký công việc"),
//    ASSIGN_TASK("Giao việc"),
//    APPROVE_TASK("Phê duyệt công việc đăng ký"),
//    REJECT_TASK("Từ chối công việc đăng ký"),
//    REVIEW_TASK("Đánh giá công việc"),
//    REQUEST_REVIEW("Gửi đánh giá công việc"),
//
//    NOTIFICATIONS("Thông báo thông qua email, SMS, thông báo"),
//
//    SIGN_IMAGE("Ký ảnh"),
//    CLOUD_CA("CA Cloud"),
//    USB_CA("USB"),
//    SIM_CA("Sim CA"),
    ;
    private final String function;

    FunctionType(String function) {
        this.function = function;
    }
    public String getFunction() {
        return function;
    }
    public static List<String> getAllFunction() {
        return Arrays.stream(values())
                .map(FunctionType::getFunction)
                .collect(Collectors.toList());
    }
    public static FunctionType fromFunction(String function) {
        return Arrays.stream(values())
                .filter(type -> type.getFunction().equalsIgnoreCase(function))
                .findFirst()
                .orElse(null);
    }
}
