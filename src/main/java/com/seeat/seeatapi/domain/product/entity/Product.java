package com.seeat.seeatapi.domain.product.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.global.common.BaseEntity;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String origin;

    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "is_mandatory_auction", nullable = false)
    private boolean isMandatoryAuction = false;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "auction_deadline")
    private LocalDateTime auctionDeadline; // [신규] 위판 마감 시각

    @Column(columnDefinition = "TEXT")
    private String description; // [신규] 상품 상세 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    protected Product() {
    }

    // 기존 테스트 코드 호환성을 위한 오버로딩 생성자 (10개 파라미터)
    public Product(Member seller, Category category, String name, String origin,
                   String storageType, BigDecimal weight, String weightUnit,
                   boolean isMandatoryAuction, BigDecimal price, int stockQuantity) {
        this(seller, category, name, origin, storageType, weight, weightUnit,
                isMandatoryAuction, price, stockQuantity, null, null);
    }

    // 2-1 상품 등록 생성자 (12개 파라미터)
    public Product(Member seller, Category category, String name, String origin,
                   String storageType, BigDecimal weight, String weightUnit,
                   boolean isMandatoryAuction, BigDecimal price, int stockQuantity,
                   LocalDateTime auctionDeadline, String description) {
        this.seller = seller;
        this.category = category;
        this.name = name;
        this.origin = origin;
        this.storageType = storageType;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.isMandatoryAuction = isMandatoryAuction;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.auctionDeadline = auctionDeadline;
        this.description = description;
        this.status = ProductStatus.PENDING_REVIEW;
    }

    // 2-5 상품 정보 수정 (null이 아닌 필드만 갱신)
    public void updateInfo(String name, String origin, String storageType,
                           BigDecimal weight, String weightUnit,
                           BigDecimal price, Integer stockQuantity,
                           LocalDateTime auctionDeadline, String description) {
        if (name != null) this.name = name;
        if (origin != null) this.origin = origin;
        if (storageType != null) this.storageType = storageType;
        if (weight != null) this.weight = weight;
        if (weightUnit != null) this.weightUnit = weightUnit;
        if (price != null) this.price = price;
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (auctionDeadline != null) this.auctionDeadline = auctionDeadline;
        if (description != null) this.description = description;
    }

    // [신규] 판매 상태 변경 (판매중/품절/판매중지)
    public void changeStatus(ProductStatus status) {
        if (status == ProductStatus.PENDING_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "판매자가 직접 설정할 수 없는 상태입니다.");
        }
        this.status = status;
    }

    // 4-1 주문 생성 시 즉시 차감 (v2.1 확정 정책)
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        this.stockQuantity -= quantity;
    }

    // 4-7, 4-8 주문 취소 시 즉시 복원
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.seller.getUserId().equals(memberId);
    }

    public Long getProductId() {
        return productId;
    }

    public Member getSeller() {
        return seller;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public String getStorageType() {
        return storageType;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public boolean isMandatoryAuction() {
        return isMandatoryAuction;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public LocalDateTime getAuctionDeadline() {
        return auctionDeadline;
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
    }
}