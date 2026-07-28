package com.seeat.seeatapi.domain.auth.controller;

import com.seeat.seeatapi.domain.auth.dto.request.*;
import com.seeat.seeatapi.domain.auth.dto.response.*;
import com.seeat.seeatapi.domain.auth.service.AuthService;
import com.seeat.seeatapi.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1-1 회원가입
    @SecurityRequirements
    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입이 완료되었습니다."));
    }

    // 1-2 일반 회원 로그인
    @SecurityRequirements
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인에 성공했습니다."));
    }

    // 1-3 관리자 계정 로그인
    @SecurityRequirements
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response, "관리자 로그인에 성공했습니다."));
    }

    // 1-4 판매자 사업자 인증 (신청/재신청 공용)
    @PostMapping("/auth/verify-business")
    public ResponseEntity<ApiResponse<VerifyBusinessResponse>> verifyBusiness(
            @com.seeat.seeatapi.global.security.CurrentMemberId Long memberId,
            @Valid @RequestBody VerifyBusinessRequest request
    ) {
        VerifyBusinessResponse response = authService.verifyBusiness(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "사업자 인증이 완료되었습니다."));
    }

    // 1-5 관리자 OTP 최초 등록
    @SecurityRequirements
    @PostMapping("/admin/otp/setup")
    public ResponseEntity<ApiResponse<OtpSetupResponse>> setupOtp(@Valid @RequestBody OtpSetupRequest request) {
        OtpSetupResponse response = authService.setupOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP 등록을 위해 QR코드를 스캔해주세요."));
    }

    // 1-6 관리자 OTP 등록 확인
    @SecurityRequirements
    @PostMapping("/admin/otp/verify")
    public ResponseEntity<ApiResponse<Object>> verifyOtpSetup(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtpSetup(request);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("otpRegistered", true), "OTP 등록이 완료되었습니다."));
    }
}