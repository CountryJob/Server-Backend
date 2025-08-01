package com.example.farm4u.dto.admin;


import java.time.LocalDate;

// 1. 대시보드 - 일별 통계
public class DashboardDto {
    private LocalDate date;
    private int totalWorkers;       // 누적 구직자 수
    private int totalFarmers;       // 누적 농가 수
    private int dailyJobPostings;   // 일일 신규 공고 수
    private int dailyMatches;       // 일일 신규 매칭(지원) 수

    // getters, setters
}