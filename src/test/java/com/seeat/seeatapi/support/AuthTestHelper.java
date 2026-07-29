package com.seeat.seeatapi.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.seeatapi.domain.auth.dto.request.LoginRequest;
import com.seeat.seeatapi.domain.auth.dto.request.SignupRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTestHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AuthTestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    // 회원가입 + 로그인 후 accessToken 반환
    public String signupAndLogin(String email, String password, String role) throws Exception {

        SignupRequest signupRequest = new SignupRequest(
                email,
                password,
                role,
                "테스트유저",
                "010-1234-5678"
        );

        // 회원가입 성공 확인
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, password);

        // 로그인 성공 확인
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        return objectMapper.readTree(responseBody)
                .path("data")
                .path("accessToken")
                .asText();
    }
}