package com.seeat.seeatapi.domain.order.dto.request;

// 4-7 구매자 취소 / 4-8 관리자 환불 공용 (reason은 DB 미저장, 로그성)
public record OrderCancelRequest(
        String reason
) {}