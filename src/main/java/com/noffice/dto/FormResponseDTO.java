package com.noffice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Getter
@Setter
public class FormResponseDTO {
    private LocalDateTime at;
    private Map<String, String> responses;

    @JsonProperty("at")
    public void setAt(long timestamp) {
        this.at = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
    }

    @Override
    public String toString() {
        return "FormResponse{" +
                "at=" + at +
                ", responses=" + responses +
                '}';
    }
}
