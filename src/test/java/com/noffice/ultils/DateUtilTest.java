package com.noffice.ultils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.ParseException;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtil - FULLY STABLE & DETERMINISTIC TESTS")
class DateUtilTest {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    // Thời gian cố định cho toàn bộ test suite
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 5, 27, 10, 0, 0);
    private static final Instant FIXED_INSTANT = FIXED_NOW.atZone(ZONE_VN).toInstant();
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZONE_VN);

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Mỗi test đều có "now" riêng, lấy từ Clock cố định
        this.now = LocalDateTime.now(FIXED_CLOCK);
    }

    // ====================================================================
    // formatTimeDiff - ỔN ĐỊNH 100%
    // ====================================================================
    @Test
    void shouldReturn_GanDay_KhiDuoi60Giay() {
        LocalDateTime createTime = now.minusSeconds(30);
        assertEquals("Gần đây", DateUtil.formatTimeDiff(createTime, now));
    }

    @ParameterizedTest
    @CsvSource({
            "1,  1 phút trước",
            "30, 30 phút trước",
            "59, 59 phút trước"
    })
    void shouldReturn_PhutTruoc(long minusMinutes, String expected) {
        LocalDateTime createTime = now.minusMinutes(minusMinutes);
        assertEquals(expected, DateUtil.formatTimeDiff(createTime, now));
    }

    @ParameterizedTest
    @CsvSource({
            "1,  1 giờ trước",
            "12, 12 giờ trước",
            "23, 23 giờ trước"
    })
    void shouldReturn_GioTruoc(long minusHours, String expected) {
        LocalDateTime createTime = now.minusHours(minusHours);
        assertEquals(expected, DateUtil.formatTimeDiff(createTime, now));
    }

    @Test
    void shouldReturn_NgayDinhDang_KhiQua24h() {
        LocalDateTime createTime = now.minusDays(5);
        assertEquals("22/05/2025", DateUtil.formatTimeDiff(createTime, now));
    }


    @Test
    void formatLocalDateToString_ShouldReturn_ddMMYYYY() {
        LocalDate date = LocalDate.of(2025, 3, 5);
        assertEquals("05/03/2025", DateUtil.formatLocalDateToString(date));
    }

    @Test
    void formatLocalDateToString_ShouldReturnNull_WhenNull() {
        assertNull(DateUtil.formatLocalDateToString(null));
    }

    @Test
    void convertStringDateFormat_shouldConvert_yyyyMMdd_HHmmssS_to_ddMMyyyy() {
        String input = "2025-05-27 14:30:45.0";
        assertEquals("27/05/2025", DateUtil.convertStringDateFormat(input));
    }

    @Test
    void convertStringDateFormat_shouldFallbackTo_ISO_WhenPrimaryFormatFails() {
        String iso = "2025-05-27T14:30:45+07:00";
        assertEquals("27/05/2025", DateUtil.convertStringDateFormat(iso));
    }

    @Test
    void convertStringDateFormat_shouldReturnOriginal_WhenBothFormatsFail() {
        String weird = "abc-123";
        assertEquals("abc-123", DateUtil.convertStringDateFormat(weird));
    }

    @Test
    void convertStringDateFormat_shouldReturnNull_WhenInputNullOrEmpty() {
        assertNull(DateUtil.convertStringDateFormat(null));
        assertNull(DateUtil.convertStringDateFormat("   "));
    }

//    @Test
//    void parseFlexibleDate2_shouldParse_yyyyMMdd_Successfully() throws ParseException {
//        java.util.Date date = DateUtil.parseFlexibleDate2("2025-05-27");
//        LocalDateTime expected = LocalDate.of(2025, 5, 27).atStartOfDay();
//        assertEquals(expected.atZone(ZONE_VN).toInstant(), date.toInstant());
//    }
//
//    @Test
//    void parseFlexibleDate2_shouldParse_ddMMyyyy_Successfully() throws ParseException {
//        java.util.Date date = DateUtil.parseFlexibleDate2("27/05/2025");
//        LocalDateTime expected = LocalDate.of(2025, 5, 27).atStartOfDay();
//        assertEquals(expected.atZone(ZONE_VN).toInstant(), date.toInstant());
//    }

    @Test
    void parseFlexibleDate2_shouldThrow_ParseException_WhenInvalidFormat() {
        ParseException ex = assertThrows(ParseException.class,
                () -> DateUtil.parseFlexibleDate2("invalid-date"));
        assertEquals("error.DateParseError", ex.getMessage());
    }

    @Test
    void parseFlexibleDate2_shouldReturnNull_WhenInputNullOrEmpty() throws ParseException {
        assertNull(DateUtil.parseFlexibleDate2(null));
        assertNull(DateUtil.parseFlexibleDate2(""));
    }
}