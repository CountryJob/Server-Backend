package com.example.farm4u.dto.admin;

import java.time.LocalDateTime;

// 6. 신고 리스트/검색 DTO
public class ReportSummaryDto {
    private Long reportId;
    private String reporterName;
    private String reportedName;
    private String reasonSummary; // 신고 내용("지각,불성실" 등)
    private LocalDateTime reportDate;
    private String reportState;

    // getters, setters
}
