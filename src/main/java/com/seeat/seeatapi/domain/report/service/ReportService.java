package com.seeat.seeatapi.domain.report.service;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.product.entity.Product;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.domain.report.dto.request.ReportCreateRequest;
import com.seeat.seeatapi.domain.report.dto.request.ReportStatusChangeRequest;
import com.seeat.seeatapi.domain.report.dto.response.ReportCreateResponse;
import com.seeat.seeatapi.domain.report.dto.response.ReportStatusChangeResponse;
import com.seeat.seeatapi.domain.report.entity.Report;
import com.seeat.seeatapi.domain.report.repository.ReportRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public ReportService(
            ReportRepository reportRepository,
            MemberRepository memberRepository,
            ProductRepository productRepository
    ) {
        this.reportRepository = reportRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    // 6-1 신고 등록
    @Transactional
    public ReportCreateResponse createReport(Member reporter, ReportCreateRequest request) {
        boolean isUserTarget = "USER".equals(request.targetType());

        Report report;
        if (isUserTarget) {
            Member targetMember = memberRepository.findById(request.targetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
            report = Report.forUser(reporter, targetMember, request.reason());
        } else {
            Product targetProduct = productRepository.findById(request.targetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
            report = Report.forProduct(reporter, targetProduct, request.reason());
        }

        reportRepository.save(report);

        return new ReportCreateResponse(report.getReportId(), request.targetType(), report.getStatus().name());
    }

    // 6-2 신고 처리 (관리자)
    @Transactional
    public ReportStatusChangeResponse changeStatus(Long reportId, ReportStatusChangeRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        report.process(request.status());

        return new ReportStatusChangeResponse(report.getReportId(), report.getStatus().name());
    }
}