package com.example.farm4u.dto.admin;

import java.time.LocalDateTime;

// 3-1. 농가 상세 DTO
public class FarmDetailDto {
    private Long userId;
    private String phoneNumber;
    private String currentMode;
    private String farmName;
    private String businessNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer areaSize;
    private String description;
    private Double avgCommunicationRating;
    private Double avgEnvironmentRating;
    private Double avgClarityRating;
    private Double avgRewardRating;
    private int reviewCount;
    private int reportCount;
    private int trustScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters, setters
}