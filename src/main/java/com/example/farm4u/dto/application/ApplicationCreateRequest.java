package com.example.farm4u.dto.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationCreateRequest {
    private Long jobId;       // 지원할 공고 id
    private Integer priority; // (선택)
}