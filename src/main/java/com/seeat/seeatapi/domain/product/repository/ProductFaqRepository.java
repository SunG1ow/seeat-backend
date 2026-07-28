package com.seeat.seeatapi.domain.product.repository;

import com.seeat.seeatapi.domain.product.entity.ProductFaq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFaqRepository extends JpaRepository<ProductFaq, Long> {
    Page<ProductFaq> findByProductProductId(Long productId, Pageable pageable);
}