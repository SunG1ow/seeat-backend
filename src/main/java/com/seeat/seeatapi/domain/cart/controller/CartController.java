package com.seeat.seeatapi.domain.cart.controller;

import com.seeat.seeatapi.domain.cart.dto.request.CartItemAddRequest;
import com.seeat.seeatapi.domain.cart.dto.response.CartItemAddResponse;
import com.seeat.seeatapi.domain.cart.dto.response.CartResponse;
import com.seeat.seeatapi.domain.cart.service.CartService;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 3-1 장바구니 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(memberId)));
    }

    // 3-2 장바구니 상품 추가
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemAddResponse>> addItem(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemAddResponse response = cartService.addItem(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "장바구니에 담았습니다."));
    }

    // 3-3 장바구니 상품 삭제
    @DeleteMapping("/items/{cartProductId}")
    public ResponseEntity<ApiResponse<Object>> removeItem(
            @CurrentMemberId Long memberId,
            @PathVariable Long cartProductId
    ) {
        cartService.removeItem(memberId, cartProductId);
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니에서 삭제되었습니다."));
    }
}