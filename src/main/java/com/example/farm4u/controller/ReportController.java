package com.example.farm4u.controller;

import com.example.farm4u.dto.report.ReportDto;
import com.example.farm4u.dto.report.ReportCreateRequest;
import com.example.farm4u.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) { this.reportService = reportService; }

    /** 신고 등록 (+집계) */
    @PostMapping
    public ResponseEntity<ReportDto> createReport(@AuthenticationPrincipal Long userId, @RequestBody ReportCreateRequest req) {
        return ResponseEntity.ok(reportService.createReport(userId, req));
    }

    /** 신고 리스트 전체조회 (ADMIN용) */
    @GetMapping
    public ResponseEntity<List<ReportDto>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
