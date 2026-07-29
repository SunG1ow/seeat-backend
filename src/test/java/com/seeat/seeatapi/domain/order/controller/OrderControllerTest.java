package com.seeat.seeatapi.domain.order.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

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
    @DisplayName("4-5 구매내역 조회 - 인증 필요, 토큰 없으면 401")
    void getMyOrders_fail_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4-5 구매내역 조회 - 인증 성공 시 200")
    void getMyOrders_success() throws Exception {
        String token = authTestHelper.signupAndLogin(
                "test@example.com",
                "password123!",
                "BUYER"
        );

        mockMvc.perform(get("/api/v1/users/me/orders")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andDo(print()) // 콘솔에 Request/Response/Error 로그 즉시 출력
                .andExpect(status().isOk());
    }
}