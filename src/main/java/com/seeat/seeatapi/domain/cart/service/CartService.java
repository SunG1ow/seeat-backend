package com.seeat.seeatapi.domain.cart.service;

import com.seeat.seeatapi.domain.cart.dto.request.CartItemAddRequest;
import com.seeat.seeatapi.domain.cart.dto.response.CartItemAddResponse;
import com.seeat.seeatapi.domain.cart.dto.response.CartResponse;
import com.seeat.seeatapi.domain.cart.entity.Cart;
import com.seeat.seeatapi.domain.cart.entity.CartItem;
import com.seeat.seeatapi.domain.cart.repository.CartItemRepository;
import com.seeat.seeatapi.domain.cart.repository.CartRepository;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MemberRepository memberRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    // 3-1 장바구니 조회 (자동 생성)
    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);

        List<CartResponse.CartItemResponse> items = cartItemRepository.findByCartCartId(cart.getCartId()).stream()
                .map(item -> new CartResponse.CartItemResponse(
                        item.getCartProductId(),
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice()
                ))
                .collect(Collectors.toList());

        return new CartResponse(cart.getCartId(), items);
    }

    // 3-2 장바구니 상품 추가
    @Transactional
    public CartItemAddResponse addItem(Long userId, CartItemAddRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        int quantity = request.quantity() != null ? request.quantity() : 1;

        CartItem cartItem = new CartItem(cart, product, quantity);
        cartItemRepository.save(cartItem);

        return new CartItemAddResponse(cartItem.getCartProductId(), product.getProductId(), quantity);
    }

    // 3-3 장바구니 상품 삭제
    @Transactional
    public void removeItem(Long userId, Long cartProductId) {
        CartItem cartItem = cartItemRepository.findById(cartProductId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByMemberUserId(userId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
                    return cartRepository.save(new Cart(member));
                });
    }
}
