package com.seeat.seeatapi.domain.order.dto.response;

import java.time.LocalDateTime;

// 4-8 관리자 환불 응답 (notifiedAt 포함이 4-7과의 차이)
public record OrderRefundResponse(
        Long orderId,
        String orderStatus,
        LocalDateTime notifiedAt
) {}