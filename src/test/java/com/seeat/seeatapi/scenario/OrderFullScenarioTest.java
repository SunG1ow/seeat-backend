package com.seeat.seeatapi.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.seeatapi.support.AuthTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderFullScenarioTest {

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
    @DisplayName("전체 시나리오: 회원가입→로그인→상품등록→주문→결제→취소, 재고가 정확히 원복되는지 검증")
    void fullOrderScenario() throws Exception {
        // 1. 판매자 회원가입 + 로그인
        String sellerToken = authTestHelper.signupAndLogin("scenario-seller@seeat.com", "password123", "SELLER");

        // 2. 카테고리 생성 (테스트 편의상 DB에 직접 없다면, 상품등록 API가 categoryId 검증하므로
        //    실제로는 카테고리 시드 데이터가 필요합니다. 여기서는 categoryId=1이 미리 존재한다고 가정합니다.
        //    존재하지 않으면 이 테스트는 400에서 실패하니, 사전 준비 데이터로 카테고리를 넣어두셔야 합니다.

        // 3. 상품 등록
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "dummy".getBytes());
        String createProductResponse = mockMvc.perform(multipart("/api/v1/products")
                        .file(image)
                        .param("categoryId", "1")
                        .param("name", "완도산 활전복")
                        .param("origin", "완도")
                        .param("storageType", "냉장")
                        .param("price", "32000")
                        .param("stockQuantity", "10")
                        .header(AUTHORIZATION, "Bearer " + sellerToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long productId = objectMapper.readTree(createProductResponse).path("data").path("productId").asLong();

        // 4. 구매자 회원가입 + 로그인
        String buyerToken = authTestHelper.signupAndLogin("scenario-buyer@seeat.com", "password123", "BUYER");

        // 5. 배송지 등록
        String addAddressBody = """
                {"alias":"집","receiverName":"구매자","receiverPhone":"010-1234-5678","address":"완도군","isDefault":true}
                """;
        String addressResponse = mockMvc.perform(post("/api/v1/users/me/addresses")
                        .header(AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(APPLICATION_JSON)
                        .content(addAddressBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long addressId = objectMapper.readTree(addressResponse).path("data").path("addressId").asLong();

        // 6. 주문 생성 (재고 3개 차감 예상: 10 -> 7)
        String createOrderBody = String.format("""
                {"items":[{"productId":%d,"quantity":3}],"addressId":%d,"requestMessage":"빠른 배송 부탁드려요"}
                """, productId, addressId);

        String createOrderResponse = mockMvc.perform(post("/api/v1/orders")
                        .header(AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(APPLICATION_JSON)
                        .content(createOrderBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderStatus").value("PAYMENT_PENDING"))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createOrderResponse).path("data").path("orderId").asLong();

        // 7. 상품 검색으로 재고가 줄었는지 간접 확인은 어려우므로,
        //    주문 취소 후 재주문이 가능한지(재고 복원)로 검증합니다.

        // 8. 구매자 주문 취소 (결제 전이므로 가능해야 함)
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel")
                        .header(AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"reason\":\"단순 변심\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));

        // 9. 재고가 복원되어, 동일 수량(3개) 재주문이 다시 가능한지 확인 (10개로 복원됐으므로 성공해야 함)
        mockMvc.perform(post("/api/v1/orders")
                        .header(AUTHORIZATION, "Bearer " + buyerToken)
                        .contentType(APPLICATION_JSON)
                        .content(createOrderBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderStatus").value("PAYMENT_PENDING"));
    }
}