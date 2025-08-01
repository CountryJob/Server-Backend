package com.example.farm4u.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AutoRateReviewRequest {
    private Long targetId;          // 점수 갱신 대상 user_id
    private Long reviewId;          // 리뷰 DB의 primary key
    private String targetType;      // 대상 유형: "WORKER" 또는 "FARMER"
}