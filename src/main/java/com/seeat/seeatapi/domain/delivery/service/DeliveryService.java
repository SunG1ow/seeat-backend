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
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderItemRepository orderItemRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, OrderItemRepository orderItemRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public void upsertTrackingInfo(Order order, String carrier, String trackingNumber) {
        Delivery delivery = deliveryRepository.findByOrderOrderId(order.getOrderId())
                .orElseGet(() -> deliveryRepository.save(new Delivery(order)));

        delivery.updateTrackingInfo(carrier, trackingNumber);
    }

    // 4-6 주문 내역 및 배송 추적
    public PageResponse<DeliveryTrackingResponse> getMyDeliveries(
            Long buyerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;

        Page<DeliveryTrackingResponse> page = deliveryRepository
                .findByBuyerAndPeriod(buyerId, startDateTime, endDateTime, pageable)
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