package com.seeat.seeatapi.domain.order.controller;

import com.seeat.seeatapi.domain.order.dto.request.OrderCancelRequest;
import com.seeat.seeatapi.domain.order.dto.OrderStatusChangeRequest;
import com.seeat.seeatapi.domain.order.dto.response.OrderRefundResponse;
import com.seeat.seeatapi.domain.order.dto.response.OrderStatusChangeResponse;
import com.seeat.seeatapi.domain.order.service.OrderService;
import com.seeat.seeatapi.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 4-3 주문 상태 실시간 변경 (관리자)
    @PatchMapping("/api/v1/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> changeStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusChangeRequest request
    ) {
        OrderStatusChangeResponse response = orderService.changeStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 상태가 변경되었습니다."));
    }

    // 4-8 관리자 주문 취소(환불)
    @PostMapping("/api/v1/admin/orders/{orderId}/refund")
    public ResponseEntity<ApiResponse<OrderRefundResponse>> refund(
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequest request
    ) {
        OrderCancelRequest req = request != null ? request : new OrderCancelRequest(null);
        OrderRefundResponse response = orderService.cancelByAdmin(orderId, req);
        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소(환불) 처리되었습니다."));
    }
}