// ReviewDto.java
package com.example.farm4u.dto.review;

import com.example.farm4u.entity.Review;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long jobId;
    private Long workerUserId;

    private Integer sincerityRating;
    private Integer promiseRating;
    private Integer skillRating;
    private Integer rehireRating;

    private Integer communicationRating;
    private Integer environmentRating;
    private Integer clarityRating;
    private Integer rewardRating;

    private String content;
    private String createdAt;
    private String updatedAt;

    public ReviewDto(Review review) {
        this.id = review.getId();
        this.userId = review.getUserId();
        this.jobId = review.getJobId();
        this.workerUserId = review.getWorkerUserId();
        this.sincerityRating = review.getSincerityRating();
        this.promiseRating = review.getPromiseRating();
        this.skillRating = review.getSkillRating();
        this.rehireRating = review.getRehireRating();
        this.communicationRating = review.getCommunicationRating();
        this.environmentRating = review.getEnvironmentRating();
        this.clarityRating = review.getClarityRating();
        this.rewardRating = review.getRewardRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt() != null ? review.getCreatedAt().toString() : null;
        this.updatedAt = review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null;
    }
}
