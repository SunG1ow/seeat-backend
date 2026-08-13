package com.seeat.seeatapi.domain.product.repository;

import com.seeat.seeatapi.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductRepositoryCustom {

    // 2-3 필터 및 정렬 검색
    Page<Product> search(
            String category,
            String origin,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String storageType,
            String sort,
            Pageable pageable
    );

    // [신규] 판매자 상품 목록 조회
    Page<Product> findBySeller(Long sellerId, Pageable pageable);
}