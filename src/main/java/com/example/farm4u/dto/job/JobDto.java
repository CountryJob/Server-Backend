package com.example.farm4u.dto.job;

import com.example.farm4u.entity.Job;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class JobDto {
    private Long id;                          // 공고 id (job_postings.id)
    private Long userId;                      // 농가 user_id (farmers.user_id)
    private String title;                     // 공고 제목
    private String description;               // 작업 내용
    private String address;                   // 주소
    private Integer areaSize;                 // 작업 면적 (m^2)
    private String startDate;                 // 작업 시작일 (YYYY-MM-DD)
    private String endDate;                   // 작업 종료일 (YYYY-MM-DD)
    private String startTime;                 // 작업 시작 시간 (HH:mm:ss)
    private String endTime;                   // 작업 종료 시간 (HH:mm:ss)
    private Boolean closed;                   // 마감 여부 (true: 마감, false: 모집중)

    private Integer salaryMale;               // 남자 일급
    private Integer salaryFemale;             // 여자 일급

    private Boolean meal;                     // 중식 제공 여부
    private Boolean snack;                    // 간식 제공 여부
    private Boolean transportAllowance;       // 교통비 제공 여부

    private Integer recruitCountMale;         // 모집 남 인원
    private Integer recruitCountFemale;       // 모집 여 인원

    private Boolean experienceRequired;       // 경험자 필요 여부

    private String createdAt;                 // 생성일 (YYYY-MM-DD HH:mm:ss)
    private String updatedAt;                 // 수정일 (YYYY-MM-DD HH:mm:ss)
    private Boolean deleted;                  // 삭제여부

    private Double aiScore;

    // 엔티티 → DTO 변환 생성자
    public JobDto(Job job) {
        this.id = job.getId();
        this.userId = job.getUserId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.address = job.getAddress();
        this.areaSize = job.getAreaSize();
        this.startDate = job.getStartDate() != null
                ? job.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
        this.endDate = job.getEndDate() != null
                ? job.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
        this.startTime = job.getStartTime() != null
                ? job.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
        this.endTime = job.getEndTime() != null
                ? job.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
        this.closed = job.getClosed();
        this.salaryMale = job.getSalaryMale();
        this.salaryFemale = job.getSalaryFemale();
        this.meal = job.getMeal();
        this.snack = job.getSnack();
        this.transportAllowance = job.getTransportAllowance();
        this.recruitCountMale = job.getRecruitCountMale();
        this.recruitCountFemale = job.getRecruitCountFemale();
        this.experienceRequired = job.getExperienceRequired();
        this.createdAt = job.getCreatedAt() != null
                ? job.getCreatedAt().toString() : null; // 필요시 포맷 맞춰 변환
        this.updatedAt = job.getUpdatedAt() != null
                ? job.getUpdatedAt().toString() : null;
        this.deleted = job.getDeleted();
        this.aiScore = job.getAiScore();
    }
}
