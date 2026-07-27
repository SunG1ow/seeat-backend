package com.seeat.seeatapi.domain.report.dto.request;

import com.seeat.seeatapi.global.common.validation.ValidEnum;
import com.seeat.seeatapi.domain.report.entity.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 6-1 신고 등록
public record ReportCreateRequest(
        @NotBlank @ValidEnum(enumClass = ReportTargetType.class) String targetType,
        @NotNull Long targetId,
        @NotBlank String reason
) {}