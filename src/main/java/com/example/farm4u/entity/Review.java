package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor @Builder
@NoArgsConstructor
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint (name = "unique_user_target",
                columnNames = {"user_id", "job_id"})
}) // TODO: indexes
public class Review extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드만
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // 농가→근로자(farmer2worker) 평가 항목
    @Column(name = "sincerity_rating", columnDefinition = "INT CHECK (sincerity_rating BETWEEN 1 AND 5)")
    private Integer sincerityRating;
    @Column(name = "promise_rating", columnDefinition = "INT CHECK (promise_rating BETWEEN 1 AND 5)")
    private Integer promiseRating;
    @Column(name = "skill_rating", columnDefinition = "INT CHECK (skill_rating BETWEEN 1 AND 5)")
    private Integer skillRating;
    @Column(name = "rehire_rating", columnDefinition = "INT CHECK (rehire_rating BETWEEN 1 AND 5)")
    private Integer rehireRating;

    // 근로자→농가(worker2farm) 평가 항목
    @Column(name = "communication_rating", columnDefinition = "INT CHECK (communication_rating BETWEEN 1 AND 5)")
    private Integer communicationRating;
    @Column(name = "environment_rating", columnDefinition = "INT CHECK (environment_rating BETWEEN 1 AND 5)")
    private Integer environmentRating;
    @Column(name = "clarity_rating", columnDefinition = "INT CHECK (clarity_rating BETWEEN 1 AND 5)")
    private Integer clarityRating;
    @Column(name = "reward_rating", columnDefinition = "INT CHECK (reward_rating BETWEEN 1 AND 5)")
    private Integer rewardRating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    @PrePersist
    protected void init(){
        if (deleted == null) deleted = false;
    }

}
