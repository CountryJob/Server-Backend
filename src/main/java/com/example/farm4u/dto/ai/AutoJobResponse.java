package com.example.farm4u.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutoJobResponse {
    private String title;                     // 공고 제목
    private String description;               // 작업 내용
    private String startDate;                 // 작업 시작일 (YYYY-MM-DD)
    private String endDate;                   // 작업 종료일 (YYYY-MM-DD)
    private String startTime;                 // 작업 시작 시간 (HH:mm:ss)
    private String endTime;                   // 작업 종료 시간 (HH:mm:ss)
    private Integer salaryMale;               // 남자 일급
    private Integer salaryFemale;             // 여자 일급
    private Boolean meal;                     // 중식 제공 여부
    private Boolean snack;                    // 간식 제공 여부
    private Boolean transportAllowance;       // 교통비 제공 여부
    private Integer recruitCountMale;         // 모집 남 인원
    private Integer recruitCountFemale;       // 모집 여 인원
    private Boolean experienceRequired;       // 경험자 필요 여부
    private Integer areaSize;                 // 작업 면적 (m^2)
}
