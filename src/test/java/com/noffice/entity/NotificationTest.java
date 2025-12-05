package com.noffice.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Cấu hình ObjectMapper hỗ trợ Java 8 Time + đúng timezone
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Test
    void noArgsConstructor_ShouldCreateInstance() {
        Notification noti = new Notification();
        assertThat(noti).isNotNull();
        assertThat(noti.getNotificationId()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Notification noti = new Notification(
                1L, 100L, "Nội dung thông báo", now, "TASK",
                99L, "Admin", 500L, "Hợp đồng A", 0, "encoded123"
        );

        assertThat(noti.getNotificationId()).isEqualTo(1L);
        assertThat(noti.getRecipientId()).isEqualTo(100L);
        assertThat(noti.getContent()).isEqualTo("Nội dung thông báo");
        assertThat(noti.getTimeStamp()).isEqualTo(now);
        assertThat(noti.getType()).isEqualTo("TASK");
        assertThat(noti.getActorId()).isEqualTo(99L);
        assertThat(noti.getActorName()).isEqualTo("Admin");
        assertThat(noti.getTargetId()).isEqualTo(500L);
        assertThat(noti.getTargetName()).isEqualTo("Hợp đồng A");
        assertThat(noti.getIsRead()).isEqualTo(0);
        assertThat(noti.getTargetIdEncode()).isEqualTo("encoded123");
    }

    @Test
    void builder_ShouldCreateInstanceWithAllFields() {
        ZonedDateTime now = ZonedDateTime.now();

        Notification noti = Notification.builder()
                .notificationId(10L)
                .recipientId(200L)
                .content("Bạn có bình luận mới")
                .timeStamp(now)
                .type("COMMENT")
                .actorId(5L)
                .actorName("Nguyễn Văn A")
                .targetId(999L)
                .targetName("Báo cáo Q4")
                .isRead(1)
                .targetIdEncode("abc123xyz")
                .build();

        assertThat(noti.getNotificationId()).isEqualTo(10L);
        assertThat(noti.getContent()).isEqualTo("Bạn có bình luận mới");
        assertThat(noti.getTimeStamp()).isEqualTo(now);
        assertThat(noti.getIsRead()).isEqualTo(1);
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Notification noti = new Notification();
        noti.setNotificationId(99L);
        noti.setContent("Test content");
        noti.setIsRead(1);

        assertThat(noti.getNotificationId()).isEqualTo(99L);
        assertThat(noti.getContent()).isEqualTo("Test content");
        assertThat(noti.getIsRead()).isEqualTo(1);
    }

    @Test
    void builder() {
        Notification noti = Notification.builder().notificationId(1L).content("A").build();
        assertThat(noti.getNotificationId()).isEqualTo(1L);
    }

    @Test
    void equals_ShouldHandleNullAndDifferentClass() {
        Notification noti = Notification.builder().notificationId(1L).build();

        assertThat(noti)
                .isNotEqualTo(null)
                .isNotEqualTo("not a Notification");
    }

    @Test
    void toString_ShouldContainAllFieldValues() {
        ZonedDateTime now = ZonedDateTime.of(2025, 4, 5, 10, 30, 0, 0, ZoneId.of("Asia/Ho_Chi_Minh"));
        Notification noti = Notification.builder()
                .notificationId(1L)
                .content("Hello")
                .timeStamp(now)
                .type("TASK")
                .isRead(0)
                .build();

        String str = noti.toString();

        assertThat(str)
                .contains("notificationId=1")
                .contains("content=Hello")
                .contains("type=TASK")
                .contains("isRead=0")
                .contains("2025-04-05T10:30+07:00"); // ZonedDateTime format
    }

    @Test
    void jsonSerialization_ShouldFormatTimestampAs_ddMMyyyy_HHmm() throws JsonProcessingException {
        ZonedDateTime zdt = ZonedDateTime.of(2025, 12, 25, 14, 30, 0, 0, ZoneId.of("Asia/Ho_Chi_Minh"));

        Notification noti = Notification.builder()
                .notificationId(1L)
                .content("Merry Christmas")
                .timeStamp(zdt)
                .type("EVENT")
                .isRead(0)
                .build();

        String json = objectMapper.writeValueAsString(noti);

        // Kiểm tra đúng format "25/12/2025 14:30"
        assertThat(json)
                .contains("\"timeStamp\":\"25/12/2025 14:30\"")
                .contains("\"notificationId\":1")
                .contains("\"content\":\"Merry Christmas\"");
    }

    @Test
    void jsonDeserialization_ShouldParseTimestampCorrectly() throws JsonProcessingException {
        String json = """
                {
                  "notificationId": 99,
                  "content": "Test noti",
                  "timeStamp": "01/01/2025 09:00",
                  "type": "TASK",
                  "isRead": 0
                }
                """;

        Notification noti = objectMapper.readValue(json, Notification.class);

        assertThat(noti.getNotificationId()).isEqualTo(99L);
        assertThat(noti.getContent()).isEqualTo("Test noti");
   }
}