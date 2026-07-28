package com.seeat.seeatapi.domain.order.controller;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.order.dto.request.OrderCancelRequest;
import com.seeat.seeatapi.domain.order.dto.request.OrderCreateRequest;
import com.seeat.seeatapi.domain.order.dto.request.PaymentRequest;
import com.seeat.seeatapi.domain.order.dto.response.*;
import com.seeat.seeatapi.domain.order.service.OrderService;
import com.seeat.seeatapi.domain.payment.service.PaymentService;
import com.seeat.seeatapi.domain.delivery.dto.response.DeliveryTrackingResponse;
import com.seeat.seeatapi.domain.delivery.service.DeliveryService;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.response.PageResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final MemberRepository memberRepository;

    public OrderController(
            OrderService orderService,
            PaymentService paymentService,
            DeliveryService deliveryService,
            MemberRepository memberRepository
    ) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
        this.memberRepository = memberRepository;
    }

    // 4-1 주문서 작성
    @PostMapping("/api/v1/orders")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @CurrentMemberId Long buyerId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        OrderCreateResponse response = orderService.createOrder(buyerId, buyer, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "주문이 생성되었습니다. 결제를 진행해주세요."));
    }

    // 4-2 결제 시도/승인 (PaymentService가 전담)
    @PostMapping("/api/v1/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.pay(orderId, request.paymentMethod(), request.pgTransactionId());
        return ResponseEntity.ok(ApiResponse.success(response, "결제가 완료되었습니다."));
    }

    // 4-4 주문 상태 이력 조회
    @GetMapping("/api/v1/orders/{orderId}/status-history")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getStatusHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getStatusHistory(orderId)));
    }

    // 4-5 사용자 구매 내역 조회
    @GetMapping("/api/v1/users/me/orders")
    public ResponseEntity<PageResponse<OrderHistoryResponse>> getMyOrders(
            @CurrentMemberId Long buyerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(buyerId, pageable));
    }

    // 4-6 주문 내역 및 배송 추적
    @GetMapping("/api/v1/users/me/delivery")
    public ResponseEntity<PageResponse<DeliveryTrackingResponse>> getMyDeliveries(
            @CurrentMemberId Long buyerId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(buyerId, startDate, endDate, pageable));
    }

    // 4-7 구매자 주문 취소
    @PostMapping("/api/v1/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelByBuyer(
            @CurrentMemberId Long buyerId,
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequest request
    ) {
        OrderCancelRequest req = request != null ? request : new OrderCancelRequest(null);
        OrderCancelResponse response = orderService.cancelByBuyer(buyerId, orderId, req);
        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소되었습니다."));
    }
}