package com.seeat.seeatapi.domain.member.entity;

import com.seeat.seeatapi.global.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "otp_secret_key")
    private String otpSecretKey;

    @Column(name = "is_withdrawn", nullable = false)
    private boolean isWithdrawn = false;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    protected Member() {
        // JPA 기본 생성자
    }

    // 회원가입 시 사용하는 생성자
    public Member(String email, String passwordHash, MemberRole role, String nickname, String phoneNumber) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    // 5-1 프로필 수정
    public void updateProfile(String nickname, String phoneNumber, String profileImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    // 5-5 비밀번호 변경
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    // 1-5 관리자 OTP 최초 등록
    public void registerOtpSecret(String otpSecretKey) {
        this.otpSecretKey = otpSecretKey;
    }

    // 5-4 회원 탈퇴 (소프트 삭제)
    public void withdraw() {
        this.isWithdrawn = true;
        this.withdrawnAt = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getOtpSecretKey() {
        return otpSecretKey;
    }

    public boolean isWithdrawn() {
        return isWithdrawn;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }
}