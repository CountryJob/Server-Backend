package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Entity
@Table(name = "applications",
        uniqueConstraints = {@UniqueConstraint(name = "unique_user_job", columnNames = {"user_id", "job_id"})})
public class Application extends BaseEntity { // TODO: indexes

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 직접 매핑 (workers 내 데이터 필요함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Worker worker;

    // 직접 매핑 (job_postings 내 데이터 필요함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_status", nullable = false)
    private ApplyStatus applyStatus = ApplyStatus.APPLIED;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer priority = 0;

    @Column(nullable = false)
    private Boolean deleted = false;

    public enum ApplyStatus{
        APPLIED, MATCHED, REJECTED, CANCELED
    }

    @PrePersist
    protected void init(){
        if (applyStatus == null) applyStatus = ApplyStatus.APPLIED;
        if (priority == null) priority = 0;
        if (deleted == null) deleted = false;
    }

}
