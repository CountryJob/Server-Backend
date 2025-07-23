package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor @Builder
@NoArgsConstructor
@Entity
@Table(name = "reports") // TODO: indexes
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드만
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportingReason reason;

    public enum ReportingReason{
        ABSENCE, POOR_PERFORMANCE, BAD_MANNER, FALSE_INFO,
        VERBAL_ABUSE, INACCURATE_INFO, UNPAID, OTHER_MISCONDUCT
    }
}
