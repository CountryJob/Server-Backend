package com.example.farm4u.dto.admin;

import java.time.LocalDateTime;

// 4. 공고 리스트/검색 DTO
public class JobSummaryDto {
    private Long jobId;
    private LocalDateTime createdAt;
    private String title;
    private String farmName;
    private String businessNumber;
    private String addressSummary;
    private int applicationCount;

    // getters, setters
}

