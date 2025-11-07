package com.noffice.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.noffice.entity.EmailConfig;
import com.noffice.repository.EmailConfigRepository;
import com.noffice.repository.UserRepository;
import com.noffice.ultils.Constants.NOTIFICATION_TYPE;

@Component
public class EmailService {
    @Lazy
    @Autowired
    private UserService userService;
    @Autowired
    private OTPCodeService otpCodeService;
    @Autowired
    private EmailConfigRepository emailConfigRepository;
    @Autowired
    private UserRepository userRepository;
    private Map<String, String> otpStorage = new HashMap<>();
    private SecureRandom random = new SecureRandom();

    public void sendEmail(String type, String email, Map<String, String> params) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email is null or empty. Skipping email sending.");
            return;
        }

        EmailConfig emailConfig = emailConfigRepository.findById(1).orElse(null);
        if (emailConfig == null) {
            throw new RuntimeException("Email configuration not found");
        }

        String template = getEmailTemplate(type);
        if (template == null) {
            throw new RuntimeException("Email template not found for type: " + type);
        }

        String content = replacePlaceholders(template, params);

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", emailConfig.isSmtpAuth() ? "true" : "false");
        properties.put("mail.smtp.starttls.enable", emailConfig.isSmtpSecure() ? "true" : "false");
        properties.put("mail.smtp.host", emailConfig.getHost());
        properties.put("mail.smtp.port", emailConfig.getPort());

        javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
            }
        });

        try {
        	javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(emailConfig.getFromEmail()));
            message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email));
            message.setSubject(getSubject(type));
            message.setContent(content, "text/html; charset=UTF-8");

            javax.mail.Transport.send(message);
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }
    }

    private String getEmailTemplate(String type) {
        switch (type) {
            case NOTIFICATION_TYPE.ADD_MORTGAGECONTRACT:
                return "Hồ sơ thế chấp mới đã được thiết lập cho [[name]].";
            case NOTIFICATION_TYPE.ADD_CUSTOMER:
                return "Hồ sơ khách hàng mới đã được thiết lập cho [[name]].";
            case NOTIFICATION_TYPE.CREATE_CREDITCONTRACT:
                return "Hợp đồng tín dụng mới đã được tạo cho [[name]].";
            case NOTIFICATION_TYPE.EXPIRE_CREDITCONTRACT:
                return "Hợp đồng tín dụng của [[name]] sắp hết hạn.";
            case NOTIFICATION_TYPE.OVERDUE_CREDITCONTRACT:
                return "Hợp đồng tín dụng của [[name]] đã quá hạn.";
            case NOTIFICATION_TYPE.APPROVE_CREDITCONTRACT:
                return "Hợp đồng tín dụng của [[name]] đã được phê duyệt.";
            case NOTIFICATION_TYPE.REJECT_CREDITCONTRACT:
                return "Hợp đồng tín dụng của [[name]] đã bị từ chối.";
            case NOTIFICATION_TYPE.COMMENT_CREDITCONTRACT:
                return "Có ý kiến mới về hợp đồng tín dụng của [[name]]: [[comment]].";
            case NOTIFICATION_TYPE.REGIS_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc đã đăng ký cho anh/chị có thông tin dưới đây:</p>
                    <p>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung công việc:</strong> [[taskContent]]<br>
                        <strong>Hạn xử lý:</strong> [[deadline]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.ASSIGN_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc đã được giao cho anh/chị có thông tin dưới đây:</p>
                    <p>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung công việc:</strong> [[taskContent]]<br>
                        <strong>Hạn xử lý:</strong> [[deadline]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.DENIED_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc của anh/chị bị từ chối với thông tin dưới đây:</p>
                    <p>
                        <strong>Người từ chối:</strong> [[denierName]]<br>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Lý do từ chối:</strong> [[denyReason]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.APPROVE_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc của anh/chị đã được phê duyệt với thông tin dưới đây:</p>
                    <p>
                        <strong>Người phê duyệt:</strong> [[approverName]]<br>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung công việc:</strong> [[taskContent]]<br>
                        <strong>Hạn xử lý:</strong> [[deadline]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.EXPIRE_TASK:
                return "Công việc của [[name]] đã hết hạn.";
            case NOTIFICATION_TYPE.SUBMIT_EVALUATION:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc của anh/chị cần được đánh giá với thông tin dưới đây:</p>
                    <p>
                        <strong>Người gửi đánh giá:</strong> [[evaluatedName]]<br>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung công việc:</strong> [[taskContent]]<br>
                        <strong>Hạn xử lý:</strong> [[deadline]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.EVALUATED_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc của anh/chị đã được đánh giá với thông tin dưới đây:</p>
                    <p>
                        <strong>Người đánh giá:</strong> [[evaluatorName]]<br>
                        <strong>Người được đánh giá:</strong> [[evaluatedName]]<br>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung đánh giá:</strong> [[evaluationContent]]<br>
                        <strong>Kết quả đánh giá:</strong> [[evaluationResult]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.REPROCESS_TASK:
                return """
                    <p>Kính gửi <strong>[[name]]</strong>,</p>
                    <p>Công việc của anh/chị cần xử lý lại với thông tin dưới đây:</p>
                    <p>
                        <strong>Người thực hiện:</strong> [[performerName]]<br>
                        <strong>Tên công việc:</strong> [[taskName]]<br>
                        <strong>Nội dung công việc:</strong> [[taskContent]]<br>
                        <strong>Hạn xử lý:</strong> [[deadline]]
                    </p>
                    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
                """;
            case NOTIFICATION_TYPE.COMMENT_TASK:
                return "Có ý kiến mới về công việc của [[name]]: [[comment]].";
            case NOTIFICATION_TYPE.TRIAL_REGISTER:
            	return """
            		    <p>Xin chào <strong>[[name]]</strong>,</p>
            		    Chúng tôi đã nhận được yêu cầu đăng ký dùng thử từ bạn. Đơn của bạn đang chờ xét duyệt, đội ngũ sẽ liên hệ bạn trong thời gian sớm nhất
            		    <p>
            		      Mọi thắc mắc, vui lòng liên hệ:<br>
            		      📧 <a href="mailto:[[mailto]]">[[mailto]]</a><br>
            		      📞 Hotline: [[Hotline]]
            		    </p>
            		    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
            		    """;
            case NOTIFICATION_TYPE.TRIAL_APPROVE:
            	return """
            		    <p>Xin chào <strong>[[name]]</strong>,</p>
            		    <p>Chúng tôi rất vui thông báo rằng yêu cầu <strong>đăng ký dùng thử</strong> của bạn đã được phê duyệt.</p>
                         <p style="font-size: 16px; font-weight: bold;">
                          <span style="font-size: 20px; vertical-align: middle;">👉</span>
                          <a href="[[website]]" target="_blank" style="font-size: 16px; font-weight: bold;">
                            Hệ thống phần mềm quản lý Quỹ tín dụng số - QTDS
                          </a> để thao tác.
                       </p>
            		    [[content]]
                        <p>
                           <span style="color: red; font-size: 16px; font-weight: bold;">
                             <span style="font-size: 20px; vertical-align: middle;">👉</span>
                             Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu.
                           </span>
                            </span><br>
                            ⏳ Thời hạn dùng thử: <strong>[[expired]]</strong>
                             </span><br>
                            📄 <a href="[[link_hdsd]]" target="_blank" style="font-size: 14px; font-weight: bold;">
                                Tài liệu hướng dẫn sử dụng hệ thống
                                </a>
                        </p>
            		    <p>
            		      Mọi thắc mắc, vui lòng liên hệ:<br>
            		      📧 <a href="mailto:[[mailto]]">[[mailto]]</a><br>
            		      📞 Hotline: [[Hotline]]
            		    </p>
            		    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
            		    """;
            case NOTIFICATION_TYPE.RECEIVE_TRIAL:
                return "<p><strong>Xin chào Admin,</strong></p>\n"
                		+ "\n"
                		+ "<p>Bạn vừa nhận được một đơn <strong>đăng ký dùng thử hệ thống</strong> từ khách hàng.</p>\n"
                		+ "\n"
                		+ "<p><strong>Thông tin chi tiết:</strong></p>\n"
                		+ "<ul>\n"
                		+ "  <li><strong>Họ tên:</strong> [[fullName]]</li>\n"
                		+ "  <li><strong>Email:</strong> [[email]]</li>\n"
                		+ "  <li><strong>Số điện thoại:</strong> [[phone]]</li>\n"
                		+ "  <li><strong>Công ty:</strong> [[companyName]]</li>\n"
                		+ "  <li><strong>Nội dung:</strong> [[note]]</li>\n"
                		+ "</ul>\n"
                		+ "\n"
                		+ "<p>Hãy kiểm tra và duyệt đơn đăng ký nếu hợp lệ.</p>\n"
                		+ "\n"
                		+ "<p>Trân trọng,<br><strong>Hệ thống QTD Platform</strong></p>\n"
                		;
            case NOTIFICATION_TYPE.ACCOUNT_EMPLOYEE:
            	return """
            		    <p>Xin chào <strong>[[name]]</strong>,</p>
            		    <p>Thông tin tài khoản dùng thử – Đăng nhập và bắt đầu trải nghiệm</p>
            		    <p>Bạn có thể truy cập hệ thống tại:</p>
                        <p style="font-size: 16px; font-weight: bold;">
                           <span style="font-size: 20px; vertical-align: middle;">👉</span>
                            <a href="[[website]]" target="_blank" style="font-size: 16px; font-weight: bold;">
                              Hệ thống phần mềm quản lý Quỹ tín dụng số - QTDS
                            </a> để thao tác.
                         </p>
            		    [[content]]
                        <p>
                         <span style="color: red; font-size: 16px; font-weight: bold;">
                           <span style="font-size: 20px; vertical-align: middle;">👉</span>
                           Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu.
                         </span>
                       </p>
            		    <p>
            		      Mọi thắc mắc, vui lòng liên hệ:<br>
            		      📧 <a href="mailto:[[mailto]]">[[mailto]]</a><br>
            		      📞 Hotline: [[Hotline]]
            		      <br>
            		       📄 <a href="[[link_hdsd]]" target="_blank" style="font-size: 14px; font-weight: bold;">
                                Tài liệu hướng dẫn sử dụng hệ thống
                                </a>
            		    </p>
            		    <p>Trân trọng,<br><strong>Đội ngũ Hỗ trợ QTD Platform</strong></p>
            		    """;
            default:
                return null;
        }
    }

    private String replacePlaceholders(String template, Map<String, String> params) {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("[[" + entry.getKey() + "]]", entry.getValue());
        }
        return result;
    }

    private String getSubject(String type) {
        switch (type) {
            case NOTIFICATION_TYPE.ADD_MORTGAGECONTRACT:
                return "[QTD] Yêu cầu xử lý hợp đồng thế chấp";
            case NOTIFICATION_TYPE.ADD_CUSTOMER:
                return "[QTD] Yêu cầu xử lý tài liệu thông tin khách hàng";
            case NOTIFICATION_TYPE.CREATE_CREDITCONTRACT:
                return "Thông báo: Tạo hợp đồng tín dụng";
            case NOTIFICATION_TYPE.EXPIRE_CREDITCONTRACT:
                return "Cảnh báo: Hợp đồng tín dụng sắp hết hạn";
            case NOTIFICATION_TYPE.OVERDUE_CREDITCONTRACT:
                return "Cảnh báo: Hợp đồng tín dụng quá hạn";
            case NOTIFICATION_TYPE.APPROVE_CREDITCONTRACT:
                return "Thông báo: Hợp đồng tín dụng được phê duyệt";
            case NOTIFICATION_TYPE.REJECT_CREDITCONTRACT:
                return "Thông báo: Hợp đồng tín dụng bị từ chối";
            case NOTIFICATION_TYPE.COMMENT_CREDITCONTRACT:
                return "Thông báo: Ý kiến về hợp đồng tín dụng";
            case NOTIFICATION_TYPE.ASSIGN_TASK:
                return "Thông báo: Giao công việc";
            case NOTIFICATION_TYPE.REGIS_TASK:
                return "Thông báo: Công việc được đăng ký";
            case NOTIFICATION_TYPE.DENIED_TASK:
                return "Thông báo: Công việc bị từ chối";
            case NOTIFICATION_TYPE.APPROVE_TASK:
                return "Thông báo: Công việc được phê duyệt";
            case NOTIFICATION_TYPE.EXPIRE_TASK:
                return "Cảnh báo: Công việc hết hạn";
            case NOTIFICATION_TYPE.SUBMIT_EVALUATION:
                return "Thông báo: Gửi đánh giá công việc";
            case NOTIFICATION_TYPE.EVALUATED_TASK:
                return "Thông báo: Công việc được đánh giá";
            case NOTIFICATION_TYPE.REPROCESS_TASK:
                return "Thông báo: Công việc cần xử lý lại";
            case NOTIFICATION_TYPE.COMMENT_TASK:
                return "Thông báo: Ý kiến về công việc";
            case NOTIFICATION_TYPE.TRIAL_REGISTER:
                return "[QTDS] Đã nhận được đơn đăng ký dùng thử";
            case NOTIFICATION_TYPE.TRIAL_APPROVE:
                return "[QTDS] Đã nhận được tài khoản dùng thử";
            case NOTIFICATION_TYPE.ACCOUNT_EMPLOYEE:
                return "[QTDS] Cấp phát tài khoản hệ thống";
            default:
                return "Thông báo hệ thống";
        }
    }
}