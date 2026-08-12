package com.seeat.seeatapi.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seeat.seeatapi.domain.order.entity.QOrderItem;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.entity.QProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Product> search(
            String category,
            String origin,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String storageType,
            String sort,
            Pageable pageable
    ) {
        QProduct product = QProduct.product;

        BooleanExpression condition = product.status.eq(
                com.seeat.seeatapi.domain.product.entity.ProductStatus.ON_SALE
        );

        if (category != null) {
            condition = condition.and(product.category.categoryName.eq(category));
        }
        if (origin != null) {
            condition = condition.and(product.origin.eq(origin));
        }
        if (priceMin != null) {
            condition = condition.and(product.price.goe(priceMin));
        }
        if (priceMax != null) {
            condition = condition.and(product.price.loe(priceMax));
        }
        if (storageType != null) {
            condition = condition.and(product.storageType.eq(storageType));
        }

        List<Product> content;

        if ("POPULAR".equals(sort)) {
            // POPULAR: order_item 누적 판매량 기준 내림차순 (v2.1 확정 정책)
            QOrderItem orderItem = QOrderItem.orderItem;

            content = queryFactory
                    .select(product)
                    .from(product)
                    .leftJoin(orderItem).on(orderItem.product.eq(product))
                    .where(condition)
                    .groupBy(product.productId)
                    .orderBy(orderItem.quantity.sum().coalesce(0).desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        } else {
            OrderSpecifier<?> orderSpecifier = resolveSort(sort, product);

            content = queryFactory
                    .selectFrom(product)
                    .where(condition)
                    .orderBy(orderSpecifier)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(condition);

        return PageableExecutionUtilsWrapper.getPage(content, pageable, countQuery::fetchOne);
    }

    // [신규] 판매자 상품 목록 조회 (상태 무관, 본인 등록 상품 전체)
    @Override
    public Page<Product> findBySeller(Long sellerId, Pageable pageable) {
        QProduct product = QProduct.product;

        BooleanExpression condition = product.seller.userId.eq(sellerId);

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(condition)
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(condition);

        return PageableExecutionUtilsWrapper.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> resolveSort(String sort, QProduct product) {
        if ("PRICE_ASC".equals(sort)) {
            return product.price.asc();
        } else if ("PRICE_DESC".equals(sort)) {
            return product.price.desc();
        }
        // LATEST(기본값): 최신 등록순
        return product.createdAt.desc();
    }
}