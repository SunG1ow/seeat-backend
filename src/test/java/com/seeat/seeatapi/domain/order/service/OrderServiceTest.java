package com.seeat.seeatapi.domain.order.service;

import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.member.repository.DeliveryAddressRepository;
import com.seeat.seeatapi.domain.order.dto.request.OrderCancelRequest;
import com.seeat.seeatapi.domain.order.dto.request.OrderCreateRequest;
import com.seeat.seeatapi.domain.order.dto.response.OrderCreateResponse;
import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.entity.OrderStatus;
import com.seeat.seeatapi.domain.order.repository.OrderItemRepository;
import com.seeat.seeatapi.domain.order.repository.OrderRepository;
import com.seeat.seeatapi.domain.order.repository.OrderStatusHistoryRepository;
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
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private DeliveryAddressRepository deliveryAddressRepository;

    @InjectMocks
    private OrderService orderService;

    private Member buyer;
    private Product product;
    private DeliveryAddress address;

    private void setUpCommon() {
        buyer = new Member("buyer@seeat.com", "encoded", MemberRole.BUYER, "구매자", "010-1234-5678");
        Category category = new Category("어류", null);
        Member seller = new Member("seller@seeat.com", "encoded", MemberRole.SELLER, "판매자", "010-9999-8888");
        product = new Product(seller, category, "완도산 활전복", "완도", "냉장",
                BigDecimal.valueOf(1.5), "kg", false, BigDecimal.valueOf(32000), 10);
        address = new DeliveryAddress(buyer, "집", "구매자", "010-1234-5678", "완도군", true);
    }

    @Test
    @DisplayName("4-1 주문 생성 성공 - 재고 즉시 차감")
    void createOrder_success_stockDecreased() {
        // given
        setUpCommon();
        when(deliveryAddressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderCreateRequest.OrderItemRequest(1L, 3)), 1L, "빠른 배송 부탁드려요"
        );

        // when
        OrderCreateResponse response = orderService.createOrder(buyer.getUserId(), buyer, request);

        // then
        assertThat(response.orderStatus()).isEqualTo("PAYMENT_PENDING");
        assertThat(product.getStockQuantity()).isEqualTo(7); // 10 - 3 즉시 차감 확인
    }

    @Test
    @DisplayName("4-1 주문 생성 실패 - 재고 부족")
    void createOrder_fail_outOfStock() {
        // given
        setUpCommon();
        when(deliveryAddressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderCreateRequest.OrderItemRequest(1L, 999)), 1L, null
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(buyer.getUserId(), buyer, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("4-7 구매자 취소 성공 - 결제 전 상태, 재고 복원")
    void cancelByBuyer_success_stockRestored() {
        // given
        setUpCommon();
        product.decreaseStock(3); // 주문 생성 시 이미 차감된 상태 시뮬레이션 (재고 7)
        Order order = new Order(buyer, address, BigDecimal.valueOf(96000), null);
        // buyer.getUserId()는 실제로는 null일 수 있어 리플렉션 없이는 직접 세팅 불가하므로,
        // isOwnedBy 검증을 위해 Mockito 스텁으로 우회
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderOrderId(1L)).thenReturn(
                List.of(new com.seeat.seeatapi.domain.order.entity.OrderItem(order, product, 3))
        );

        // when
        orderService.cancelByBuyer(buyer.getUserId(), 1L, new OrderCancelRequest("단순 변심"));

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // 7 + 3 복원 확인
    }
}