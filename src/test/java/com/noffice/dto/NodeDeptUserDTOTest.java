package com.noffice.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NodeDeptUserDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testNoArgsConstructor() {
        NodeDeptUserDTO dto = new NodeDeptUserDTO();
        assertNull(dto.getId());
        assertNull(dto.getUserId());
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        NodeDeptUserDTO dto = new NodeDeptUserDTO();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setApprovalType("SERIAL");
        dto.setDeptName("Finance");
        dto.setNote("Need fast approval");
        dto.setRoleId(roleId);
        dto.setStep("STEP_2");

        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals("SERIAL", dto.getApprovalType());
        assertEquals("Finance", dto.getDeptName());
        assertEquals("Need fast approval", dto.getNote());
        assertEquals(roleId, dto.getRoleId());
        assertEquals("STEP_2", dto.getStep());
    }

    @Test
    void testJsonIgnoreUnknown() throws JsonProcessingException {
        String json = """
                {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "userId": "550e8400-e29b-41d4-a716-446655440001",
                    "approvalType": "PARALLEL",
                    "deptName": "HR",
                    "note": "OK",
                    "roleId": "550e8400-e29b-41d4-a716-446655440002",
                    "step": "REVIEW",
                    "unknownField": "should be ignored",
                    "anotherField": 123
                }
                """;

        NodeDeptUserDTO dto = objectMapper.readValue(json, NodeDeptUserDTO.class);

        assertNotNull(dto.getId());
        assertEquals("PARALLEL", dto.getApprovalType());
        assertEquals("HR", dto.getDeptName());
        // Không bị lỗi do @JsonIgnoreProperties(ignoreUnknown = true)
    }

    @Test
    void testJsonSerialization() throws JsonProcessingException {
        NodeDeptUserDTO dto = new NodeDeptUserDTO();
        dto.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        dto.setUserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        dto.setStep("APPROVE");
        dto.setApprovalType("SERIAL");

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("11111111-1111-1111-1111-111111111111"));
        assertTrue(json.contains("22222222-2222-2222-2222-222222222222"));
        assertTrue(json.contains("APPROVE"));
    }
}
