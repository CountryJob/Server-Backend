package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "farmers") // TODO: indexes
public class Farmer extends BaseEntity {

    // ID값으로만 조회 -> User JoinColumn OneToOne 굳이 설정하지 않음
    @Id
    @Column(name = "user_id")
    private Long userId;

    // 농장주명
    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "business_number", length = 20, nullable = false, unique = true)
    private String businessNumber;

    // 농장명
    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "area_size")
    private Integer areaSize;

    @Column(columnDefinition = "TEXT")
    private String description;

    //
    @Column(name = "avg_communication_rating", precision = 3, scale = 2)
    private BigDecimal avgCommunicationRating;

    @Column(name = "avg_environment_rating", precision = 3, scale = 2)
    private BigDecimal avgEnvironmentRating;

    @Column(name = "avg_clarity_rating", precision = 3, scale = 2)
    private BigDecimal avgClarityRating;

    @Column(name = "avg_reward_rating", precision = 3, scale = 2)
    private BigDecimal avgRewardRating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "trust_score", columnDefinition = "INT CHECK(trust_score BETWEEN 0 AND 100)")
    private Integer trustScore;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void init(){
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.deleted == null) this.deleted = false;
    }
}
