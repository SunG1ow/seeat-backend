package com.seeat.seeatapi.domain.product.controller;

import com.seeat.seeatapi.domain.product.dto.response.ProductSellerListResponse;
import com.seeat.seeatapi.domain.product.service.ProductService;
import com.seeat.seeatapi.global.response.PageResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [신규] 판매자 상품 관리
@RestController
@RequestMapping("/api/v1/seller/products")
public class SellerProductController {

    private final ProductService productService;

    public SellerProductController(ProductService productService) {
        this.productService = productService;
    }

    // [신규] 판매자 본인 상품 목록 조회 (상태 무관, 전체)
    @GetMapping
    public ResponseEntity<PageResponse<ProductSellerListResponse>> getSellerProducts(
            @CurrentMemberId Long sellerId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getSellerProducts(sellerId, pageable));
    }
}