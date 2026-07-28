package com.seeat.seeatapi.domain.report.controller;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.report.dto.request.ReportCreateRequest;
import com.seeat.seeatapi.domain.report.dto.request.ReportStatusChangeRequest;
import com.seeat.seeatapi.domain.report.dto.response.ReportCreateResponse;
import com.seeat.seeatapi.domain.report.dto.response.ReportStatusChangeResponse;
import com.seeat.seeatapi.domain.report.service.ReportService;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final MemberRepository memberRepository;

    public ReportController(ReportService reportService, MemberRepository memberRepository) {
        this.reportService = reportService;
        this.memberRepository = memberRepository;
    }

    // 6-1 신고 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ReportCreateResponse>> createReport(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Member reporter = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ReportCreateResponse response = reportService.createReport(reporter, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "신고가 접수되었습니다."));
    }

    // 6-2 신고 처리 (관리자)
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<ReportStatusChangeResponse>> changeStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportStatusChangeRequest request
    ) {
        ReportStatusChangeResponse response = reportService.changeStatus(reportId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "신고가 처리되었습니다."));
    }
}