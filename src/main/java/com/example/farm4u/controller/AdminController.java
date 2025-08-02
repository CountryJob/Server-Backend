package com.example.farm4u.controller;
import com.example.farm4u.dto.admin.*;
import com.example.farm4u.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 1. 대시보드: 전체 통계/변화량 (기간 기준)
    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardDto>> getDashboard(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(adminService.getDashboardData(startDate, endDate));
    }

    // 2. 구직자 리스트 및 필터
    @GetMapping("/users")
    public ResponseEntity<List<WorkerSummaryDto>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterField) {
        return ResponseEntity.ok(adminService.getUsers(search, filterField));
    }

    // 2-1. 구직자 상세
    @GetMapping("/users/{id}")
    public ResponseEntity<WorkerDetailDto> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    // 3. 농가 리스트 및 필터
    @GetMapping("/farms")
    public ResponseEntity<List<FarmSummaryDto>> getFarms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterField) {
        return ResponseEntity.ok(adminService.getFarms(search, filterField));
    }

    // 3-1. 농가 상세
    @GetMapping("/farms/{id}")
    public ResponseEntity<FarmDetailDto> getFarmDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getFarmDetail(id));
    }

    // 4. 공고 리스트 및 필터
    @GetMapping("/jobs")
    public ResponseEntity<List<JobSummaryDto>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterField) {
        return ResponseEntity.ok(adminService.getJobs(search, filterField));
    }

    // 4-1. 공고 상세
    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobDetailDto> getJobDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getJobDetail(id));
    }

    // 5. 매칭(지원) 리스트 및 필터
    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationSummaryDto>> getApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterField) {
        return ResponseEntity.ok(adminService.getApplications(search, filterField));
    }

    // 5-1. 매칭-농가 상세
    @GetMapping("/applications/{id}/farm")
    public ResponseEntity<FarmDetailDto> getApplicationFarmDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getApplicationFarmDetail(id));
    }

    // 5-2. 매칭-구직자 상세
    @GetMapping("/applications/{id}/worker")
    public ResponseEntity<WorkerDetailDto> getApplicationWorkerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getApplicationWorkerDetail(id));
    }

    // 6. 신고 리스트 및 필터
    @GetMapping("/reports")
    public ResponseEntity<List<ReportSummaryDto>> getReports(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterField) {
        return ResponseEntity.ok(adminService.getReports(search, filterField));
    }

    // 6-1. 신고 상세
    @GetMapping("/reports/{id}")
    public ResponseEntity<ReportDetailDto> getReportDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getReportDetail(id));
    }
}