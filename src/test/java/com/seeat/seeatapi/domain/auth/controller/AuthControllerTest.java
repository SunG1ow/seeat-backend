package com.seeat.seeatapi.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.seeatapi.domain.auth.dto.request.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1-1 회원가입 API 호출 성공 - 201 응답")
    void signup_api_success() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "integration-test@seeat.com", "password123", "SELLER", "통합테스트유저", "010-9999-8888"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration-test@seeat.com"))
                .andExpect(jsonPath("$.data.role").value("SELLER"));
    }

    @Test
    @DisplayName("1-1 회원가입 API 실패 - 잘못된 role 값(400)")
    void signup_api_fail_invalidRole() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "invalid-role@seeat.com", "password123", "INVALID_ROLE", "테스트", "010-1111-2222"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}