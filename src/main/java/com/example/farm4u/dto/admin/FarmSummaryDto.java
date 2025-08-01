package com.example.farm4u.dto.admin;

// 3. 농가 요약/리스트 DTO
public class FarmSummaryDto {
    private Long userId;
    private String farmName;            // 농장명(name)
    private String ownerName;           // 관리자명(농장주인, user_name)
    private String businessNumber;
    private String phoneNumber;
    private String addressSummary;      // 주소 앞부분
    private int jobPostingCount;        // 공고 수

    // getters, setters
}
