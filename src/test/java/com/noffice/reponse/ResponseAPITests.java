package com.noffice.reponse;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseAPITests {

    @Test
    void test_ResponseAPI_Builder() {
        Object obj = UUID.randomUUID();
        ResponseAPI response = ResponseAPI.builder()
                .object(obj)
                .message("created")
                .status(201)
                .build();

        assertThat(response.getObject()).isSameAs(obj);
        assertThat(response.getMessage()).isEqualTo("created");
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    void test_ResponseAPI_NoArgsConstructor_And_Setters() {
        ResponseAPI response = new ResponseAPI();
        response.setObject(null);
        response.setMessage("error");
        response.setStatus(400);

        assertThat(response.getObject()).isNull();
        assertThat(response.getMessage()).isEqualTo("error");
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
