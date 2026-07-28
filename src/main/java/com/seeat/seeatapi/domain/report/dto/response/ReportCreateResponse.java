package com.seeat.seeatapi.domain.report.dto.response;

// 6-1 신고 등록 응답
public record ReportCreateResponse(
        Long reportId,
        String targetType,
        String status
) {}