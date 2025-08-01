package com.example.farm4u.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

// 6-1. 신고 상세 DTO
public class ReportDetailDto {
    private Long reportId;
    private Long userId; // 신고자
    private Long jobId;
    private Long workerUserId; // 피신고자가 worker일 때
    private List<String> reasons;
    private String reportState;
    private LocalDateTime createdAt;

    // getters, setters
}