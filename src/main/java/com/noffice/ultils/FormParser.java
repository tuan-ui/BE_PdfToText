package com.noffice.ultils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.dto.FormResponseDTO;

public class FormParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static FormResponseDTO parseFormContent(String json) {
        try {
            return mapper.readValue(json, FormResponseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi parse form_content", e);
        }
    }

}
