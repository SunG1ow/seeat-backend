package com.seeat.seeatapi.domain.report.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.global.common.BaseEntity;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import jakarta.persistence.*;

@Entity
@Table(name = "report")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private Member reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_product_id")
    private Product reportedProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Column(nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    protected Report() {
    }

    private Report(ReportTargetType targetType, Member reportedUser, Product reportedProduct,
                   Member reporter, String reason) {
        this.targetType = targetType;
        this.reportedUser = reportedUser;
        this.reportedProduct = reportedProduct;
        this.reporter = reporter;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    // 6-1 회원 신고
    public static Report forUser(Member reporter, Member targetMember, String reason) {
        return new Report(ReportTargetType.USER, targetMember, null, reporter, reason);
    }

    // 6-1 상품 신고
    public static Report forProduct(Member reporter, Product targetProduct, String reason) {
        return new Report(ReportTargetType.PRODUCT, null, targetProduct, reporter, reason);
    }

    // 6-2 신고 처리 (관리자)
    public void process(String newStatus) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "이미 처리된 신고입니다.");
        }
        this.status = ReportStatus.valueOf(newStatus);
    }

    public Long getReportId() {
        return reportId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public Member getReportedUser() {
        return reportedUser;
    }

    public Product getReportedProduct() {
        return reportedProduct;
    }

    public Member getReporter() {
        return reporter;
    }

    public String getReason() {
        return reason;
    }

    public ReportStatus getStatus() {
        return status;
    }
}