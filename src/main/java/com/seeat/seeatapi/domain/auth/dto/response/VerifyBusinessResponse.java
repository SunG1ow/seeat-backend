package com.seeat.seeatapi.domain.auth.dto.response;

import java.time.LocalDateTime;

// 1-4 사업자 인증 응답
public record VerifyBusinessResponse(
        boolean verified,
        String authStatus,
        LocalDateTime verifiedAt
) {}