package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Category;

import java.util.List;
import java.util.stream.Collectors;

// 2-2 카테고리 트리 조회 (재귀 구조)
public record CategoryResponse(
        Long categoryId,
        String categoryName,
        Long parentCategoryId,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category category) {
        List<CategoryResponse> childResponses = category.getChildren().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());

        Long parentId = category.getParentCategory() != null
                ? category.getParentCategory().getCategoryId()
                : null;

        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                parentId,
                childResponses
        );
    }
}