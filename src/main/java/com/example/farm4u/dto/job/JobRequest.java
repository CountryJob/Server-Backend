package com.example.farm4u.dto.job;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

// Job 생성/수정 요청용 DTO
@Getter
@Setter
@NoArgsConstructor
public class JobRequest {
    private String title;                    // 공고 제목
    private Integer areaSize;                // 작업 면적 (m^2)
    private LocalDate startDate;                // 시작일 (YYYY-MM-DD)
    private LocalDate endDate;                  // 종료일 (YYYY-MM-DD)
    private LocalTime startTime;                // 시작 시각 (예: "09:00:00")
    private LocalTime endTime;                  // 종료 시각 (예: "18:00:00")
    //
    private Integer salaryMale;              // 남자 일급
    private Integer salaryFemale;            // 여자 일급
    private Boolean meal;                    // 중식 제공 여부
    private Boolean snack;                   // 간식 제공 여부
    private Boolean transportAllowance;      // 교통비 제공 여부
    private Integer recruitCountMale;        // 남자 모집 인원
    private Integer recruitCountFemale;      // 여자 모집 인원
    private Boolean experienceRequired;      // 경험자 필요 여부
    private String description;              // 작업 내용(상세 설명)
    private String address;                  // 작업 위치(주소)

}
