package com.seeat.seeatapi.domain.settlement.service;

import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.settlement.dto.response.SettlementResponse;
import com.seeat.seeatapi.domain.settlement.entity.Settlement;
import com.seeat.seeatapi.domain.settlement.entity.SettlementStatus;
import com.seeat.seeatapi.domain.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("7-2 정산 내역 조회 - status 필터 적용")
    void getMySettlements_success_filterByStatus() {
        // given
        Order mockOrder = mock(Order.class);
        when(mockOrder.getOrderId()).thenReturn(100L);

        Settlement pending = mock(Settlement.class);
        when(pending.getStatus()).thenReturn(SettlementStatus.PENDING);
        when(pending.getSettlementId()).thenReturn(1L);
        when(pending.getAmount()).thenReturn(BigDecimal.valueOf(50000));
        when(pending.getOrder()).thenReturn(mockOrder);
        when(pending.getSettledAt()).thenReturn(null);

        Settlement completed = mock(Settlement.class);
        when(completed.getStatus()).thenReturn(SettlementStatus.COMPLETED);

        when(settlementRepository.findBySellerUserId(1L)).thenReturn(List.of(pending, completed));

        // when
        List<SettlementResponse> result = settlementService.getMySettlements(1L, "PENDING");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("PENDING");
        assertThat(result.get(0).orderId()).isEqualTo(100L);
    }
}