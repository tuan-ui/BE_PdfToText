package com.noffice.ultils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "hello,        hello",
            "hel/lo,       hel//lo",
            "hel_lo,       hel/_lo",
            "hel%lo,       hel/%lo",
    })
    void escapeSql_ShouldEscapeSpecialChars(String input, String expected) {
        assertEquals(expected, StringUtils.escapeSql(input));
    }

    @Test
    void toLikeAndLowerCaseString_ShouldAddPercentAndEscape() {
        assertEquals("%hel/_lo%", StringUtils.toLikeAndLowerCaseString("Hel_lo"));
    }

    @ParameterizedTest
    @CsvSource({
            "Hà Nội, Ha Noi",
            "Trần Văn Đạt, Tran Van Dat",
            "Phạm Thị Ngọc Ánh, Pham Thi Ngoc Anh",
            "đã Đã ĐẠI, da Da DAI"
    })
    void unAccent_ShouldRemoveAccentsCorrectly(String input, String expected) {
        assertEquals(expected, StringUtils.unAccent(input));
    }

    @Test
    void removeAccents_ShouldLowerCaseAndRemoveD() {
        assertEquals("ha noi", StringUtils.removeAccents("Hà Nội"));
        assertEquals("tran van dat", StringUtils.removeAccents("Trần Văn Đạt"));
    }


}