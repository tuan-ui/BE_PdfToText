package com.noffice.ultils;

import com.noffice.dto.FormResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class FormParserTest {

    private static final String REAL_JSON = """
        {"at":1762327310682,"responses":{"Tiêu đề: Input":"cq"}}
        """;

    private static final String FULL_JSON_VIETNAMESE = """
        {
          "at": 1735680000000,
          "responses": {
            "Họ và tên: *": "Nguyễn Văn A",
            "Email:": "nguyenvana@example.com",
            "Số điện thoại:": "0901234567",
            "Bạn đến từ tỉnh thành nào?:": "Hà Nội"
          }
        }
        """;

    @Test
    void parseRealJson_ShouldConvertTimestampToLocalDateTime() {
        FormResponseDTO result = FormParser.parseFormContent(REAL_JSON);

        assertNotNull(result);
        assertNotNull(result.getAt());
        assertNotNull(result.getResponses());

        // Kiểm tra timestamp được convert đúng (1762327310682 ≈ 2025-10-15 14:35:10 GMT+7)
        LocalDateTime expected = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(1762327310682L),
                ZoneId.systemDefault()
        );

        assertEquals(expected, result.getAt());
        assertEquals("cq", result.getResponses().get("Tiêu đề: Input"));

    }

    @Test
    void parseJson_WithVietnameseKeys_ShouldWorkPerfectly() {
        FormResponseDTO result = FormParser.parseFormContent(FULL_JSON_VIETNAMESE);

        assertNotNull(result);
        assertNotNull(result.getAt());
        assertEquals(4, result.getResponses().size());

        assertEquals("Nguyễn Văn A", result.getResponses().get("Họ và tên: *"));
        assertEquals("nguyenvana@example.com", result.getResponses().get("Email:"));
        assertEquals("0901234567", result.getResponses().get("Số điện thoại:"));
        assertEquals("Hà Nội", result.getResponses().get("Bạn đến từ tỉnh thành nào?:"));

    }


    @Test
    void parseNull_ShouldThrowException() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> FormParser.parseFormContent(null)
        );
        assertEquals("Lỗi khi parse form_content", ex.getMessage());
    }
}