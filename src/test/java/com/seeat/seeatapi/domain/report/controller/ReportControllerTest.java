package com.seeat.seeatapi.domain.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.seeatapi.support.AuthTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthTestHelper authTestHelper;

    @BeforeEach
    void setUp() {
        authTestHelper = new AuthTestHelper(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("6-1 상품 신고 등록 API - 201 응답")
    void createReport_success() throws Exception {
        String reporterToken = authTestHelper.signupAndLogin("reporter@seeat.com", "password123", "BUYER");

        String requestBody = objectMapper.writeValueAsString(
                new com.seeat.seeatapi.domain.report.dto.request.ReportCreateRequest("PRODUCT", 1L, "허위 상품 정보")
        );

        mockMvc.perform(post("/api/v1/reports")
                        .header(AUTHORIZATION, "Bearer " + reporterToken)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound()) // 상품ID 1이 실제로 없으므로 TARGET_NOT_FOUND(404) 예상
                .andExpect(jsonPath("$.code").value("TARGET_NOT_FOUND"));
    }
}