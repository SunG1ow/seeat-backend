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

    public SellerBusinessInfo(Member member, String businessRegistrationNumber,
                              String representativeName, LocalDate openingDate) {
        this.member = member;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.representativeName = representativeName;
        this.openingDate = openingDate;
        this.authStatus = AuthStatus.PENDING;
    }

    public void reapply(String businessRegistrationNumber, String representativeName, LocalDate openingDate) {
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.representativeName = representativeName;
        this.openingDate = openingDate;
        this.authStatus = AuthStatus.PENDING;
        this.verifiedAt = null;
    }

    public void verify() {
        this.authStatus = AuthStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        this.authStatus = AuthStatus.REJECTED;
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