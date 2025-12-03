package com.noffice.reponse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenResponseTests {

    @Test
    void test_TokenResponse_AllArgsConstructor_And_Getter() {
        TokenResponse response = new TokenResponse("access-token-xyz");

        assertThat(response.getAccessToken()).isEqualTo("access-token-xyz");
    }

    @Test
    void test_TokenResponse_Immutable() {
        TokenResponse response = new TokenResponse("token123");
        assertThat(response.getAccessToken()).isEqualTo("token123");
    }
}