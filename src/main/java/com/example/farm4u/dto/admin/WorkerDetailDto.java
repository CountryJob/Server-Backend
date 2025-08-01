package com.example.farm4u.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 2-1. 구직자 정보 상세 DTO
public class WorkerDetailDto {
    private Long userId;
    private String phoneNumber;
    private String currentMode;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private Double latitude;
    private Double longitude;
    private String activeArea;
    private String workType;
    private List<String> workDays;
    private Boolean hasFarmExperience;
    private List<String> farmExperienceTypes;
    private List<String> farmExperienceTasks;
    private String workIntensity;
    private Double avgSincerityRating;
    private Double avgPromiseRating;
    private Double avgSkillRating;
    private Double avgRehireRating;
    private int reviewCount;
    private int reportCount;
    private int trustScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters, setters
}
