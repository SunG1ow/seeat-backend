package com.seeat.seeatapi.domain.report.service;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.product.repository.ProductRepository;
import com.seeat.seeatapi.domain.report.dto.request.ReportCreateRequest;
import com.seeat.seeatapi.domain.report.dto.request.ReportStatusChangeRequest;
import com.seeat.seeatapi.domain.report.dto.response.ReportCreateResponse;
import com.seeat.seeatapi.domain.report.entity.Report;
import com.seeat.seeatapi.domain.report.repository.ReportRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("6-1 회원 신고 등록 성공")
    void createReport_success_userTarget() {
        // given
        Member reporter = new Member("reporter@seeat.com", "encoded", MemberRole.BUYER, "신고자", "010-1111-2222");
        Member target = new Member("target@seeat.com", "encoded", MemberRole.SELLER, "피신고자", "010-3333-4444");
        ReportCreateRequest request = new ReportCreateRequest("USER", 2L, "부적절한 판매 행위");

        when(memberRepository.findById(2L)).thenReturn(Optional.of(target));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ReportCreateResponse response = reportService.createReport(reporter, request);

        // then
        assertThat(response.targetType()).isEqualTo("USER");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("6-1 신고 등록 실패 - 신고 대상 없음")
    void createReport_fail_targetNotFound() {
        // given
        Member reporter = new Member("reporter@seeat.com", "encoded", MemberRole.BUYER, "신고자", "010-1111-2222");
        ReportCreateRequest request = new ReportCreateRequest("USER", 999L, "테스트");

        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reporter, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TARGET_NOT_FOUND);
    }

    @Test
    @DisplayName("6-2 신고 처리 실패 - 이미 처리된 신고")
    void changeStatus_fail_alreadyProcessed() {
        // given
        Member reporter = new Member("reporter@seeat.com", "encoded", MemberRole.BUYER, "신고자", "010-1111-2222");
        Member target = new Member("target@seeat.com", "encoded", MemberRole.SELLER, "피신고자", "010-3333-4444");
        Report report = Report.forUser(reporter, target, "사유");
        report.process("APPROVED"); // 이미 한 번 처리됨

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportStatusChangeRequest request = new ReportStatusChangeRequest("REJECTED", null);

        // when & then
        assertThatThrownBy(() -> reportService.changeStatus(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS_TRANSITION);
    }
}