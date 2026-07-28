package com.seeat.seeatapi.domain.delivery.service;

import com.seeat.seeatapi.domain.delivery.dto.response.DeliveryTrackingResponse;
import com.seeat.seeatapi.domain.delivery.entity.Delivery;
import com.seeat.seeatapi.domain.delivery.repository.DeliveryRepository;
import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.repository.OrderItemRepository;
import com.seeat.seeatapi.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, OrderItemRepository orderItemRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // 4-3에서 SHIPPING 전환 시 호출 (carrier/trackingNumber upsert)
    @Transactional
    public void upsertTrackingInfo(Order order, String carrier, String trackingNumber) {
        Delivery delivery = deliveryRepository.findByOrderOrderId(order.getOrderId())
                .orElseGet(() -> deliveryRepository.save(new Delivery(order)));

        delivery.updateTrackingInfo(carrier, trackingNumber);
    }

    // 4-6 주문 내역 및 배송 추적
    public PageResponse<DeliveryTrackingResponse> getMyDeliveries(Long buyerId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<DeliveryTrackingResponse> page = deliveryRepository.findByBuyerAndPeriod(buyerId, startDate, endDate, pageable)
                .map(delivery -> new DeliveryTrackingResponse(
                        delivery.getOrder().getOrderId(),
                        orderItemRepository.findByOrderOrderId(delivery.getOrder().getOrderId()).stream()
                                .findFirst().map(item -> item.getProduct().getName()).orElse(""),
                        delivery.getCarrier(),
                        delivery.getTrackingNumber(),
                        delivery.getOrder().getOrderStatus().name()
                ));
        return PageResponse.of(page);
    }
}