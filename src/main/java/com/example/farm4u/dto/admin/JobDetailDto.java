package com.example.farm4u.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 4-1. 공고 상세 DTO
public class JobDetailDto {
    private Long jobId;
    private Long userId;
    private String title;
    private String description;
    private String address;
    private Integer areaSize;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime; // "09:00:00" 등
    private String endTime;
    private Boolean closed;
    private Integer salaryMale;
    private Integer salaryFemale;
    private Boolean meal;
    private Boolean snack;
    private Boolean transportAllowance;
    private Integer recruitCountMale;
    private Integer recruitCountFemale;
    private Boolean experienceRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters, setters
}
