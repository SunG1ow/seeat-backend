package com.seeat.seeatapi.domain.report.dto.response;

// 6-2 신고 처리 응답
public record ReportStatusChangeResponse(
        Long reportId,
        String status
) {}