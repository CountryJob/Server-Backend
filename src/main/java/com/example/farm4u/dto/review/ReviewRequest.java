package com.example.farm4u.dto.review;

import lombok.Getter;
import lombok.Setter;

/**
 * 후기 등록/수정 요청 DTO
 * - mode 분기를 Service에서 처리: worker(본인+jobId), farmer(본인+workerUserId+jobId)
 */
@Getter
@Setter
public class ReviewRequest {
    private Long jobId;
    private Long workerUserId; // 농가→근로자 리뷰시 필수, worker→job이면 생략/null

    private String content;

    // farmer -> worker 평가
    private Integer sincerityRating;
    private Integer promiseRating;
    private Integer skillRating;
    private Integer rehireRating;

    // worker -> job 평가
    private Integer communicationRating;
    private Integer environmentRating;
    private Integer clarityRating;
    private Integer rewardRating;
}
