package com.noffice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DnDDTOTest {
    private static final String UUID = "11111111-1111-1111-1111-111111111111";
    private static final String UUID_2 = "22222222-2222-2222-2222-222222222222";
    private JsonNode node;
    
    @BeforeEach
    void setUp() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "A");
        map.put("active", true);

        ObjectMapper mapper = new ObjectMapper();
        node = mapper.valueToTree(map);
    }
    @Test
    void noArgsConstructor_Works() {
        DnDDTO dto = new DnDDTO();
        assertNotNull(dto);
    }

    @Test
    void allArgsConstructor_SetsAllFields() {

        DnDDTO dto = new DnDDTO(
                UUID, node
        );

        assertEquals(UUID, dto.getId());
        assertEquals(node, dto.getContent());
    }

    @Test
    void getterSetter_Works() {
        DnDDTO dto = new DnDDTO();

        dto.setId(UUID);
        dto.setContent(node);

        assertEquals(UUID, dto.getId());
        assertEquals(node, dto.getContent());
    }

    @Test
    void builder_CreatesObjectCorrectly() {

        DnDDTO dto = DnDDTO.builder()
                .id(UUID)
                .content(node)
                .build();

        assertEquals(UUID, dto.getId());
        assertEquals(node, dto.getContent());
    }

    @Test
    void equalsAndHashCode_SameFields_Equal() {
        DnDDTO dto1 = DnDDTO.builder().id(UUID).content(node).build();
        DnDDTO dto2 = DnDDTO.builder().id(UUID).content(node).build();

        assertEquals(dto1.getId(), dto2.getId());
    }

    @Test
    void equals_DifferentId_NotEqual() {
        DnDDTO dto1 = DnDDTO.builder().id(UUID).build();
        DnDDTO dto2 = DnDDTO.builder().id(UUID_2).build();

        assertNotEquals(dto1, dto2);
    }
    @Test
    void toString_data() {
        DnDDTO dto = DnDDTO.builder().id(UUID).build();

        String str = dto.toString();
        assertTrue(str.contains(UUID));
    }
}
