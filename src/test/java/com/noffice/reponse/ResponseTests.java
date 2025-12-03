package com.noffice.reponse;

import com.noffice.ultils.Constants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseTests {

    @Test
    void test_Response_NoArgsConstructor_And_Setters() {
        Response response = new Response();
        response.setData("test data");
        response.setMessage(Constants.message.SUCCESS);
        response.setStatus(200);

        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isEqualTo(Constants.message.SUCCESS);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void test_Response_AllArgsConstructor_And_Getters() {
        Object data = new Object();
        Response response = new Response(data, "ok", 201);

        assertThat(response.getData()).isSameAs(data);
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    void test_Response_ToString_Equals_HashCode() {
        Response r1 = new Response("data", "msg", 200);
        Response r2 = new Response("data", "msg", 200);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.toString()).contains("data", "msg", "200");
    }
}
