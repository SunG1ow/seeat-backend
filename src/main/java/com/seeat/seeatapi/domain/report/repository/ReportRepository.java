package com.seeat.seeatapi.domain.report.repository;

import com.seeat.seeatapi.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}