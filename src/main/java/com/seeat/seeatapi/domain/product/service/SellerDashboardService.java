package com.seeat.seeatapi.domain.product.service;

import com.seeat.seeatapi.domain.order.repository.OrderRepository;
import com.seeat.seeatapi.domain.product.dto.response.SellerDashboardResponse;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class SellerDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public SellerDashboardService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // 7-1 상품 및 매출관리 대시보드
    public SellerDashboardResponse getDashboard(Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // TODO: ProductRepository에 findBySellerUserId(Long, Pageable) 메서드 추가 필요 (20단계 보완)
        Page<Product> productPage = productRepository.findAll(pageable); // 임시: 전체 조회, 판매자 필터는 추후 보완

        Page<SellerDashboardResponse.SellerProductItem> items = productPage.map(p ->
                new SellerDashboardResponse.SellerProductItem(
                        p.getProductId(), p.getName(), p.getStockQuantity(), p.getStatus().name()
                )
        );

        // TODO: 실제 기간별 매출 집계는 OrderRepository에 집계 쿼리 추가 필요 (지금은 0으로 임시 처리)
        SellerDashboardResponse.SalesSummary summary =
                new SellerDashboardResponse.SalesSummary(BigDecimal.ZERO, 0L);

        return new SellerDashboardResponse(PageResponse.of(items), summary);
    }
}