package com.seeat.seeatapi.domain.product.controller;

import com.seeat.seeatapi.domain.product.dto.response.SellerDashboardResponse;
import com.seeat.seeatapi.domain.product.service.SellerDashboardService;
import com.seeat.seeatapi.domain.settlement.dto.response.SettlementResponse;
import com.seeat.seeatapi.domain.settlement.service.SettlementService;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerDashboardService sellerDashboardService;
    private final SettlementService settlementService;

    public SellerController(SellerDashboardService sellerDashboardService, SettlementService settlementService) {
        this.sellerDashboardService = sellerDashboardService;
        this.settlementService = settlementService;
    }

    // 7-1 상품 및 매출관리 대시보드
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> getDashboard(
            @CurrentMemberId Long sellerId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ) {
        SellerDashboardResponse response = sellerDashboardService.getDashboard(sellerId, startDate, endDate, pageable);
        return org.springframework.http.ResponseEntity.ok(ApiResponse.success(response));
    }

    // 7-2 정산 내역 조회
    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlements(
            @CurrentMemberId Long sellerId,
            @RequestParam(required = false) String status
    ) {
        return org.springframework.http.ResponseEntity.ok(
                ApiResponse.success(settlementService.getMySettlements(sellerId, status))
        );
    }
}