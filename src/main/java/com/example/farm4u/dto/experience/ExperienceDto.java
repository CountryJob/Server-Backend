package com.example.farm4u.dto.experience;

import com.example.farm4u.entity.Experience;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExperienceDto {
    private Long id;               // experiences.id (PK)
    private Long userId;           // 근로자 id (workers.user_id)
    private Long applyId;          // 지원내역 id (applications.id)
    private Long jobId;            // 공고 id (job_postings.id)
    private String title;          // 공고 제목(스냅샷)
    private String address;        // 작업지 주소(스냅샷)
    private String startDate;      // 시작일 (YYYY-MM-DD)
    private String endDate;        // 종료일 (YYYY-MM-DD)
    private String startTime;      // 시작 시각 (HH:mm:ss)
    private String endTime;        // 종료 시각 (HH:mm:ss)
    private Integer salaryMale;    // 남자 임금
    private Integer salaryFemale;  // 여자 임금
    private String createdAt;      // 생성일 (YYYY-MM-DD HH:mm:ss)


    public ExperienceDto(Experience exp) {
        this.id = exp.getId();
        this.userId = exp.getUserId();
        this.applyId = exp.getApplyId();
        this.jobId = exp.getJobId();
        this.title = exp.getTitle();
        this.address = exp.getAddress();
        this.startDate = exp.getStartDate() != null ? exp.getStartDate().toString() : null;
        this.endDate = exp.getEndDate() != null ? exp.getEndDate().toString() : null;
        this.startTime = exp.getStartTime() != null ? exp.getStartTime().toString() : null;
        this.endTime = exp.getEndTime() != null ? exp.getEndTime().toString() : null;
        this.salaryMale = exp.getSalaryMale();
        this.salaryFemale = exp.getSalaryFemale();
        this.createdAt = exp.getCreatedAt() != null ? exp.getCreatedAt().toString() : null;
    }
}
