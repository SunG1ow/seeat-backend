package com.seeat.seeatapi.domain.member.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_business_info")
public class SellerBusinessInfo {

    @Id
    @Column(name = "user_id")
    private Long userId;

    // user_id가 PK이자 member로의 FK인 공유 기본키 구조 (5.2 스키마 기준)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(name = "business_registration_number", nullable = false, unique = true, length = 10)
    private String businessRegistrationNumber;

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_status", nullable = false, length = 20)
    private AuthStatus authStatus;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    protected SellerBusinessInfo() {
    }

    // 1-4 판매자 사업자 인증 최초 요청
    public SellerBusinessInfo(Member member, String businessRegistrationNumber,
                              String representativeName, LocalDate openingDate) {
        this.member = member;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.representativeName = representativeName;
        this.openingDate = openingDate;
        this.authStatus = AuthStatus.PENDING;
    }

    // 국세청 API 검증 성공
    public void verify() {
        this.authStatus = AuthStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    // 1-4 Error 422: 국세청 API 검증 결과 불일치
    public void reject() {
        this.authStatus = AuthStatus.REJECTED;
        this.verifiedAt = null;
    }

    // 재인증 시도 (upsert) - 정보 갱신 후 PENDING으로 초기화
    public void updateBusinessInfo(String businessRegistrationNumber,
                                   String representativeName, LocalDate openingDate) {
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.representativeName = representativeName;
        this.openingDate = openingDate;
        this.authStatus = AuthStatus.PENDING;
        this.verifiedAt = null;
    }

    public Long getUserId() {
        return userId;
    }

    public Member getMember() {
        return member;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public AuthStatus getAuthStatus() {
        return authStatus;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}