package com.seeat.seeatapi.domain.cart.service;

import com.seeat.seeatapi.domain.cart.dto.request.CartItemAddRequest;
import com.seeat.seeatapi.domain.cart.dto.response.CartItemAddResponse;
import com.seeat.seeatapi.domain.cart.entity.Cart;
import com.seeat.seeatapi.domain.cart.entity.CartItem;
import com.seeat.seeatapi.domain.cart.repository.CartItemRepository;
import com.seeat.seeatapi.domain.cart.repository.CartRepository;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.product.entity.Category;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("3-1 장바구니 조회 - 기존 장바구니 없으면 자동 생성")
    void getCart_success_autoCreate() {
        // given
        Member member = new Member("buyer@seeat.com", "encoded", MemberRole.BUYER, "구매자", "010-1234-5678");
        Cart newCart = new Cart(member);

        when(cartRepository.findByMemberUserId(1L)).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(cartItemRepository.findByCartCartId(any())).thenReturn(List.of());

        // when
        var response = cartService.getCart(1L);

        // then
        assertThat(response.items()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("3-2 장바구니 상품 추가 성공 - 수량 미지정 시 기본값 1")
    void addItem_success_defaultQuantity() {
        // given
        Member member = new Member("buyer@seeat.com", "encoded", MemberRole.BUYER, "구매자", "010-1234-5678");
        Cart cart = new Cart(member);
        Category category = new Category("어류", null);
        Member seller = new Member("seller@seeat.com", "encoded", MemberRole.SELLER, "판매자", "010-9999-8888");
        Product product = new Product(seller, category, "완도산 활전복", "완도", "냉장",
                BigDecimal.valueOf(1.5), "kg", false, BigDecimal.valueOf(32000), 10);

        when(cartRepository.findByMemberUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemAddRequest request = new CartItemAddRequest(5L, null);

        // when
        CartItemAddResponse response = cartService.addItem(1L, request);

        // then
        assertThat(response.quantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("3-3 장바구니 상품 삭제 실패 - 존재하지 않는 항목")
    void removeItem_fail_notFound() {
        // given
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.removeItem(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);
    }
}