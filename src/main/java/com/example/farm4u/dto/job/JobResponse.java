package com.example.farm4u.dto.job;

public class JobResponse {

    /**
     *   "work_type": "상추 수확",
     *
     *   "start_date": "2024-08-01",
     *   "end_date": "2024-08-03",
     *   "start_time": "08:00:00",
     *   "end_time": "17:00:00",
     *   "area_size": 50,
     *   "meal": true,
     *   "snack": true,
     *   "transport_allowance": false,
     *   "address_match": true,
     *   "description": "상추 밭에서 수확 작업\n상태 확인 후 포장 및 이동 보조\n..",
     *   "salary_male": 110000,
     *   "salary_female": 100000,
     *   "recruit_count_male": 2,
     *   "recruit_count_female": 1
     */

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

}
