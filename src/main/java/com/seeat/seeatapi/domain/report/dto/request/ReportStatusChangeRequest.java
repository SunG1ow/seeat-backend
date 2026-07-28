package com.seeat.seeatapi.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;

// 6-2 신고 처리 (관리자)
public record ReportStatusChangeRequest(
        @NotBlank String status,
        String adminMemo
) {}