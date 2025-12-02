package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FormResponseDTOTest {

    @Test
    void testSetAt_ShouldConvertTimestampCorrectly() {
        // given
        long timestamp = 1700000000000L;

        LocalDateTime expected = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );

        FormResponseDTO dto = new FormResponseDTO();

        dto.setAt(timestamp);
        assertThat(dto.getAt()).isEqualTo(expected);
    }

    @Test
    void testResponses_SetAndGet() {

        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");

        FormResponseDTO dto = new FormResponseDTO();

        dto.setResponses(map);
        assertThat(dto.getResponses())
                .hasSize(2)
                .containsEntry("field1", "value1")
                .containsEntry("field2", "value2");
    }

    @Test
    void testToString_ShouldContainFields() {
        FormResponseDTO dto = new FormResponseDTO();
        dto.setAt(1700000000000L);

        Map<String, String> res = new HashMap<>();
        res.put("a", "1");
        dto.setResponses(res);

        String result = dto.toString();

        assertThat(result).contains("at=").contains("responses=");
    }
}

