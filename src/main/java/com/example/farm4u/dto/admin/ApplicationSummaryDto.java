package com.example.farm4u.dto.admin;

import java.time.LocalDateTime;

// 5. 지원(매칭) 리스트/검색 DTO
public class ApplicationSummaryDto {
    private Long applicationId;
    private String jobTitle;
    private String farmName;
    private String workerName;
    private LocalDateTime applyDate;
    private String applyStatus;

    // getters, setters
}
