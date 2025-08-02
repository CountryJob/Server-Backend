package com.example.farm4u.dto.report;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportCreateRequest {
    private Long jobId;      // 신고 대상 공고 id
    private Long workerUserId;      // 농가가 worker 신고할 때만 필수, 근로자가 job 신고하면 null
    private String reason;   // 신고 사유(ENUM)
}
