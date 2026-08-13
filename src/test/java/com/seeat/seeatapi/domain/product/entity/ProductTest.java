package com.seeat.seeatapi.domain.product.entity;

import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("재고 차감 성공")
    void decreaseStock_success() {
        // given
        Product product = new Product(null, null, "완도산 활전복", "완도", "냉장",
                BigDecimal.valueOf(1.5), "kg", false, BigDecimal.valueOf(32000), 10);

        // when
        product.decreaseStock(3);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 부족 시 OUT_OF_STOCK 예외")
    void decreaseStock_fail_outOfStock() {
        // given
        Product product = new Product(null, null, "완도산 활전복", "완도", "냉장",
                BigDecimal.valueOf(1.5), "kg", false, BigDecimal.valueOf(32000), 5);

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(10))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("재고 복원 성공 (주문 취소 시)")
    void increaseStock_success() {
        // given
        Product product = new Product(null, null, "완도산 활전복", "완도", "냉장",
                BigDecimal.valueOf(1.5), "kg", false, BigDecimal.valueOf(32000), 5);
        product.decreaseStock(3);

        // when
        product.increaseStock(3);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }
}