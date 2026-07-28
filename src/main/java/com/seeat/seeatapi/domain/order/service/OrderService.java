package com.seeat.seeatapi.domain.order.service;

import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import com.seeat.seeatapi.domain.member.repository.DeliveryAddressRepository;
import com.seeat.seeatapi.domain.order.dto.request.OrderCancelRequest;
import com.seeat.seeatapi.domain.order.dto.request.OrderCreateRequest;
import com.seeat.seeatapi.domain.order.dto.OrderStatusChangeRequest;
import com.seeat.seeatapi.domain.order.dto.response.*;
import com.seeat.seeatapi.domain.order.entity.Order;
import com.seeat.seeatapi.domain.order.entity.OrderItem;
import com.seeat.seeatapi.domain.order.entity.OrderStatus;
import com.seeat.seeatapi.domain.order.entity.OrderStatusHistory;
import com.seeat.seeatapi.domain.order.repository.OrderItemRepository;
import com.seeat.seeatapi.domain.order.repository.OrderRepository;
import com.seeat.seeatapi.domain.order.repository.OrderStatusHistoryRepository;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            ProductRepository productRepository,
            DeliveryAddressRepository deliveryAddressRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.productRepository = productRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
    }

    // 4-1 주문서 작성 (재고 즉시 차감 - v2.1 확정)
    @Transactional
    public OrderCreateResponse createOrder(Long buyerId, com.seeat.seeatapi.domain.member.entity.Member buyer, OrderCreateRequest request) {
        DeliveryAddress address = deliveryAddressRepository.findById(request.addressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        List<Product> products = new java.util.ArrayList<>();

        for (OrderCreateRequest.OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            product.decreaseStock(itemRequest.quantity()); // 재고 즉시 차감, 부족시 자동 예외

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            products.add(product);
        }

        Order order = new Order(buyer, address, totalAmount, request.requestMessage());
        orderRepository.save(order);

        for (int i = 0; i < request.items().size(); i++) {
            OrderItem orderItem = new OrderItem(order, products.get(i), request.items().get(i).quantity());
            orderItemRepository.save(orderItem);
        }

        orderStatusHistoryRepository.save(new OrderStatusHistory(order, OrderStatus.PAYMENT_PENDING));

        return OrderCreateResponse.from(order);
    }


    // 4-3 관리자 주문 상태 변경
    @Transactional
    public OrderStatusChangeResponse changeStatus(Long orderId, OrderStatusChangeRequest request) {
        Order order = findOrderOrThrow(orderId);
        OrderStatus newStatus = OrderStatus.valueOf(request.status());

        order.changeStatus(newStatus);
        orderStatusHistoryRepository.save(new OrderStatusHistory(order, newStatus));

        // TODO: NotificationService 연동 (24단계 이후), Delivery upsert는 DeliveryService에서 처리

        return new OrderStatusChangeResponse(order.getOrderId(), newStatus.name(), LocalDateTime.now(), "ALIMTALK");
    }

    // 4-4 주문 상태 이력 조회
    public List<OrderStatusHistoryResponse> getStatusHistory(Long orderId) {
        return orderStatusHistoryRepository.findByOrderOrderIdOrderByChangedAtAsc(orderId).stream()
                .map(OrderStatusHistoryResponse::from)
                .collect(Collectors.toList());
    }

    // 4-5 사용자 구매 내역 조회
    public PageResponse<OrderHistoryResponse> getMyOrders(Long buyerId, Pageable pageable) {
        Page<OrderHistoryResponse> page = orderRepository.findByBuyerUserId(buyerId, pageable)
                .map(order -> new OrderHistoryResponse(
                        order.getOrderId(),
                        orderItemRepository.findByOrderOrderId(order.getOrderId()).stream()
                                .findFirst().map(item -> item.getProduct().getName()).orElse(""),
                        order.getOrderStatus().name(),
                        order.getCreatedAt()
                ));
        return PageResponse.of(page);
    }

    // 4-7 구매자 주문 취소
    @Transactional
    public OrderCancelResponse cancelByBuyer(Long buyerId, Long orderId, OrderCancelRequest request) {
        Order order = findOrderOrThrow(orderId);

        if (!order.isOwnedBy(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        order.cancelByBuyer();
        orderStatusHistoryRepository.save(new OrderStatusHistory(order, OrderStatus.CANCELLED));
        restoreStock(orderId);

        // 취소 사유는 DB 미저장, 로그로만 기록 (v2.1 확정)
        if (request.reason() != null) {
            org.slf4j.LoggerFactory.getLogger(OrderService.class)
                    .info("주문 취소 (구매자) - orderId: {}, reason: {}", orderId, request.reason());
        }

        return new OrderCancelResponse(order.getOrderId(), order.getOrderStatus().name());
    }

    // 4-8 관리자 주문 취소(환불)
    @Transactional
    public OrderRefundResponse cancelByAdmin(Long orderId, OrderCancelRequest request) {
        Order order = findOrderOrThrow(orderId);

        order.cancelByAdmin();
        orderStatusHistoryRepository.save(new OrderStatusHistory(order, OrderStatus.CANCELLED));
        restoreStock(orderId);

        if (request.reason() != null) {
            org.slf4j.LoggerFactory.getLogger(OrderService.class)
                    .info("주문 취소 (관리자 환불) - orderId: {}, reason: {}", orderId, request.reason());
        }

        // TODO: NotificationService 연동 (24단계 이후)

        return new OrderRefundResponse(order.getOrderId(), order.getOrderStatus().name(), LocalDateTime.now());
    }

    private void restoreStock(Long orderId) {
        orderItemRepository.findByOrderOrderId(orderId)
                .forEach(item -> item.getProduct().increaseStock(item.getQuantity()));
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    // OrderService.java 내부에 추가
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. id=" + orderId));
    }
}