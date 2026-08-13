package com.seeat.seeatapi.domain.delivery.service;

import com.seeat.seeatapi.domain.delivery.entity.Delivery;
import com.seeat.seeatapi.domain.delivery.repository.DeliveryRepository;
import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    @DisplayName("4-3 배송정보 upsert - 신규 생성")
    void upsertTrackingInfo_success_create() {
        // given
        Member buyer = new Member("buyer@seeat.com", "encoded", MemberRole.BUYER, "구매자", "010-1234-5678");
        DeliveryAddress address = new DeliveryAddress(buyer, "집", "구매자", "010-1234-5678", "완도군", true);
        Order order = new Order(buyer, address, BigDecimal.valueOf(32000), null);

        when(deliveryRepository.findByOrderOrderId(any())).thenReturn(Optional.empty());
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        deliveryService.upsertTrackingInfo(order, "CJ대한통운", "123456789012");

        // then
        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    @DisplayName("4-3 배송정보 upsert - 기존 배송정보 갱신")
    void upsertTrackingInfo_success_update() {
        // given
        Member buyer = new Member("buyer@seeat.com", "encoded", MemberRole.BUYER, "구매자", "010-1234-5678");
        DeliveryAddress address = new DeliveryAddress(buyer, "집", "구매자", "010-1234-5678", "완도군", true);
        Order order = new Order(buyer, address, BigDecimal.valueOf(32000), null);
        Delivery existingDelivery = new Delivery(order);

        when(deliveryRepository.findByOrderOrderId(any())).thenReturn(Optional.of(existingDelivery));

        // when
        deliveryService.upsertTrackingInfo(order, "한진택배", "987654321098");

        // then
        assertThat(existingDelivery.getCarrier()).isEqualTo("한진택배");
        assertThat(existingDelivery.getTrackingNumber()).isEqualTo("987654321098");
        verify(deliveryRepository, never()).save(any()); // 이미 영속 상태라 dirty checking으로 처리, save 호출 불필요
    }
}