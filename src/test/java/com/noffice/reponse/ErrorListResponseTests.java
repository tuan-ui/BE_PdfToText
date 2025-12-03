package com.noffice.reponse;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorListResponseTests {

    @Test
    void test_ErrorListResponse_NoArgsConstructor_And_Setters() {

        ErrorListResponse response = new ErrorListResponse();

        ErrorListResponse.ErrorResponse err = new ErrorListResponse.ErrorResponse();
        err.setId(UUID.randomUUID());
        err.setCode("ERR001");
        err.setName("Validation Error");
        err.setErrorMessage("Field is required");

        response.setErrors(List.of(err));
        response.setTotal(1);
        response.setHasError(true);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getHasError()).isTrue();
        assertThat(response.getErrors().get(0).getCode()).isEqualTo("ERR001");
    }

    @Test
    void test_ErrorListResponse_AllArgsConstructor() {
        // Kiểm tra @AllArgsConstructor
        UUID id = UUID.randomUUID();
        ErrorListResponse.ErrorResponse err = new ErrorListResponse.ErrorResponse(
                id, "ERR404", "Not Found", "Resource not found"
        );

        ErrorListResponse response = new ErrorListResponse(
                Collections.singletonList(err),
                1,
                true
        );

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getHasError()).isTrue();
        assertThat(response.getErrors().get(0).getId()).isEqualTo(id);
        assertThat(response.getErrors().get(0).getErrorMessage()).isEqualTo("Resource not found");
    }

    @Test
    void test_ErrorListResponse_Equals_HashCode_ToString() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        ErrorListResponse.ErrorResponse err = new ErrorListResponse.ErrorResponse(
                id, "E001", "Invalid", "Username required"
        );

        ErrorListResponse r1 = new ErrorListResponse(List.of(err), 1, true);
        ErrorListResponse r2 = new ErrorListResponse(List.of(err), 1, true);

        // equals & hashCode (từ @Data)
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());

        // toString (từ @Data)
        assertThat(r1.toString())
                .contains("errors")
                .contains("total=1")
                .contains("hasError=true")
                .contains("E001")
                .contains("Username required");
    }

    @Test
    void test_ErrorResponse_NoArgsConstructor_And_All_Setters() {
        // Kiểm tra @NoArgsConstructor + @Setter (rõ ràng có @Setter riêng)
        ErrorListResponse.ErrorResponse err = new ErrorListResponse.ErrorResponse();

        UUID id = UUID.randomUUID();
        err.setId(id);
        err.setCode("ERR500");
        err.setName("Server Error");
        err.setErrorMessage("Unexpected error");

        assertThat(err.getId()).isEqualTo(id);
        assertThat(err.getCode()).isEqualTo("ERR500");
        assertThat(err.getName()).isEqualTo("Server Error");
        assertThat(err.getErrorMessage()).isEqualTo("Unexpected error");
    }

    @Test
    void test_ErrorResponse_AllArgsConstructor() {
        UUID id = UUID.randomUUID();
        ErrorListResponse.ErrorResponse err = new ErrorListResponse.ErrorResponse(
                id, "DUPLICATE", "Conflict", "Already exists"
        );

        assertThat(err.getId()).isEqualTo(id);
        assertThat(err.getCode()).isEqualTo("DUPLICATE");
        assertThat(err.getName()).isEqualTo("Conflict");
        assertThat(err.getErrorMessage()).isEqualTo("Already exists");
    }

    @Test
    void test_ErrorResponse_Equals_HashCode_ToString() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        ErrorListResponse.ErrorResponse e1 = new ErrorListResponse.ErrorResponse(
                id, "E001", "Bad Request", "Invalid data"
        );
        ErrorListResponse.ErrorResponse e2 = new ErrorListResponse.ErrorResponse(
                id, "E001", "Bad Request", "Invalid data"
        );

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        assertThat(e1.toString())
                .contains("E001")
                .contains("Bad Request")
                .contains("Invalid data")
                .contains(id.toString());
    }

    @Test
    void test_ErrorResponse_EmptyList_HasError_False() {
        ErrorListResponse response = new ErrorListResponse(
                Collections.emptyList(),
                0,
                false
        );

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getTotal()).isZero();
        assertThat(response.getHasError()).isFalse();
    }
}

