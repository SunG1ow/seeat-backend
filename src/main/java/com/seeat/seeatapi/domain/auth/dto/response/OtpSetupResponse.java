package com.seeat.seeatapi.domain.auth.dto.response;

// 1-5 OTP 등록 응답
public record OtpSetupResponse(
        String otpSecretKey,
        String qrCodeUrl
) {}