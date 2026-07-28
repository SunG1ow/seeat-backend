package com.seeat.seeatapi.domain.product.repository;

import com.seeat.seeatapi.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 2-2 트리 조회: 최상위(부모 없는) 카테고리부터 시작
    List<Category> findByParentCategoryIsNull();
}