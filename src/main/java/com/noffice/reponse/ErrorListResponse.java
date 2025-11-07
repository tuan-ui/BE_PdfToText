package com.noffice.reponse;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ErrorListResponse {
    private List<ErrorResponse> errors;
    private int total;
    private Boolean hasError;
    @Data
    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ErrorResponse {
        private UUID id;
        private String code;
        private String name;
        private String errorMessage;
    }
}
