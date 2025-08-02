package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Entity
@Table(name = "experiences",
        uniqueConstraints = {@UniqueConstraint(name = "unique_user_apply", columnNames = {"user_id", "apply_id"})})
public class Experience extends BaseEntity { // TODO: indexes

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드만 선언 (객체 직접 매핑X)
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 필드만 선언 (객체 직접 매핑X)
    @Column(name = "apply_id", nullable = false)
    private Long applyId;

    // 필드만 선언 (객체 직접 매핑X)
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // denormalized fields (job 목록 접근할 일이 많음) (+ 스냅샷)
    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", columnDefinition = "TIME DEFAULT '09:00:00'")
    private LocalTime startTime = LocalTime.of(9, 0);

    @Column(name = "end_time", columnDefinition = "TIME DEFAULT '18:00:00'")
    private LocalTime endTime = LocalTime.of(18, 0);

    @Column(name = "salary_male", nullable = false)
    private Integer salaryMale;

    @Column(name = "salary_female", nullable = false)
    private Integer salaryFemale;

}