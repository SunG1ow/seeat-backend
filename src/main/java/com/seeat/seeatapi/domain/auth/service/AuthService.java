package com.seeat.seeatapi.domain.auth.service;

import com.seeat.seeatapi.domain.auth.dto.request.*;
import com.seeat.seeatapi.domain.auth.dto.response.*;
import com.seeat.seeatapi.domain.member.entity.AuthStatus;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.member.entity.SellerBusinessInfo;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.member.repository.SellerBusinessInfoRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final SellerBusinessInfoRepository sellerBusinessInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            SellerBusinessInfoRepository sellerBusinessInfoRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.sellerBusinessInfoRepository = sellerBusinessInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 1-1 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        MemberRole role = MemberRole.valueOf(request.role());
        String passwordHash = passwordEncoder.encode(request.password());

        Member member = new Member(request.email(), passwordHash, role, request.nickname(), request.phoneNumber());
        memberRepository.save(member);

        return SignupResponse.from(member);
    }

    // 1-2 일반 로그인
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getUserId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getUserId());

        return LoginResponse.of(accessToken, refreshToken, 3600, member.getRole().name());
    }

    // 1-3 관리자 로그인
    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .filter(m -> m.getRole() == MemberRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (member.getOtpSecretKey() == null || !verifyOtpCode(member.getOtpSecretKey(), request.otpCode())) {
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        String accessToken = jwtTokenProvider.createAdminAccessToken(member.getUserId(), member.getRole().name());

        return new AdminLoginResponse(accessToken, 1800, member.getRole().name());
    }

    // 1-4 사업자 인증 (신청/재신청 공용, 횟수 제한 없음 - v2.1 확정)
    @Transactional
    public VerifyBusinessResponse verifyBusiness(Long userId, VerifyBusinessRequest request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        LocalDate openingDate = LocalDate.parse(request.openingDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));

        SellerBusinessInfo businessInfo = sellerBusinessInfoRepository.findByUserId(userId)
                .map(info -> {
                    info.reapply(request.businessRegistrationNumber(), request.representativeName(), openingDate);
                    return info;
                })
                .orElseGet(() -> new SellerBusinessInfo(
                        member, request.businessRegistrationNumber(), request.representativeName(), openingDate
                ));

        // TODO: 국세청 API 실제 연동 (향후 확장). 지금은 Mock 처리.
        boolean verified = mockNtsVerification(request.businessRegistrationNumber());

        if (verified) {
            businessInfo.verify();
        } else {
            businessInfo.reject();
        }

        sellerBusinessInfoRepository.save(businessInfo);

        return new VerifyBusinessResponse(
                verified,
                businessInfo.getAuthStatus().name(),
                businessInfo.getVerifiedAt()
        );
    }

    // 1-5 관리자 OTP 최초 등록
    @Transactional
    public OtpSetupResponse setupOtp(OtpSetupRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .filter(m -> m.getRole() == MemberRole.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (member.getOtpSecretKey() != null) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_REGISTERED);
        }

        String secret = generateOtpSecret();
        member.registerOtpSecret(secret);

        String qrCodeUrl = String.format(
                "otpauth://totp/SEEAT:%s?secret=%s&issuer=SEEAT", member.getEmail(), secret
        );

        return new OtpSetupResponse(secret, qrCodeUrl);
    }

    // 1-6 관리자 OTP 등록 확인
    public boolean verifyOtpSetup(OtpVerifyRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getOtpSecretKey() == null || !verifyOtpCode(member.getOtpSecretKey(), request.otpCode())) {
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        return true;
    }

    // --- Mock/헬퍼 메서드 (실제 연동은 향후 확장 과제) ---

    private boolean mockNtsVerification(String businessRegistrationNumber) {
        return !"0000000000".equals(businessRegistrationNumber);
    }

    private String generateOtpSecret() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    private boolean verifyOtpCode(String secret, String otpCode) {
        // TODO: 실제 TOTP(RFC 6238) 알고리즘 또는 라이브러리로 교체 필요
        // 지금은 Mock: "000000"이 아니면 통과 (개발/테스트 편의용)
        return otpCode != null && !"000000".equals(otpCode);
    }
}