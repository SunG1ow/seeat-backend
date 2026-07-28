package com.seeat.seeatapi.domain.product.repository;

import com.seeat.seeatapi.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductProductIdOrderBySortOrderAsc(Long productId);
    long countByProductProductId(Long productId); // 이미지 5장 제한 검증용
}