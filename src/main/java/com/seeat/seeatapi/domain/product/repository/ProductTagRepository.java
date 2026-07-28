package com.seeat.seeatapi.domain.product.repository;

import com.seeat.seeatapi.domain.product.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    void deleteByProductProductId(Long productId); // 2-5 수정 시 태그 전체 교체용
}