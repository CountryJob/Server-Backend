// Review.java
package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_worker_review", columnNames = {"user_id", "job_id"}),
                @UniqueConstraint(name = "unique_farmer_review", columnNames = {"user_id", "worker_user_id", "job_id"})
        }
)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "worker_user_id")
    private Long workerUserId;

    @Column(name = "sincerity_rating")
    private Integer sincerityRating;
    @Column(name = "promise_rating")
    private Integer promiseRating;
    @Column(name = "skill_rating")
    private Integer skillRating;
    @Column(name = "rehire_rating")
    private Integer rehireRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;
    @Column(name = "environment_rating")
    private Integer environmentRating;
    @Column(name = "clarity_rating")
    private Integer clarityRating;
    @Column(name = "reward_rating")
    private Integer rewardRating;

    @Column(columnDefinition="TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void init(){
        if (deleted == null) deleted = false;
    }
}
