package com.example.farm4u.dto.report;

import com.example.farm4u.entity.Report;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportDto {
    private Long id;             // 신고 id
    private Long userId;         // 신고자
    private Long jobId;          // 대상 공고
    private Long workerUserId;
    private String reason;       // 신고 사유(ENUM)
    private String createdAt;

    public ReportDto(Report r) {
        this.id = r.getId();
        this.userId = r.getUserId();
        this.jobId = r.getJobId();
        this.workerUserId = r.getWorkerUserId();
        this.reason = r.getReason() != null ? r.getReason().name() : null;
        this.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
    }
}
