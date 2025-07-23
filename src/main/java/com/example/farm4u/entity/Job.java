package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor @Builder
@NoArgsConstructor
@Getter
@Entity
@Table(name = "job_postings") // TODO: indexes
public class Job extends BaseEntity {

    @PrePersist
    protected void init(){
        if (startTime == null) startTime = LocalTime.of(9, 0);
        if (endTime == null) endTime = LocalTime.of(18, 0);
        if (closed == null) closed = false;
        if (meal == null) meal = false;
        if (snack == null) snack = false;
        if (transportAllowance == null) transportAllowance = false;
        if (recruitCountMale == null) recruitCountMale = 0;
        if (recruitCountFemale == null) recruitCountFemale = 0;
        if (experienceRequired == null) experienceRequired = false;
        if (deleted == null) deleted = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    FOREIGN KEY (user_id) REFERENCES farmers(user_id)
    // -> 필요 시 User 조인
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT NOT NULL", nullable = false)
    private String description;

//    -- -> 농가 기본 정보의 address, area_size를 그대로 가져와 반영해야 함. (*)

    @Column(length = 255, nullable = false)
    private String address;

    @Column(name = "area_size", nullable = false)
    private Integer areaSize;

//     -- -> today 와 같거나 이후로 제한해야 함 (서버 단에서)

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", columnDefinition = "TIME DEFAULT '09:00:00'")
    private LocalTime startTime = LocalTime.of(9, 0);

    @Column(name = "end_time", columnDefinition = "TIME DEFAULT '18:00:00'")
    private LocalTime endTime = LocalTime.of(18, 0);

    @Column
    private Boolean closed = false;

    @Column(name = "salary_male", nullable = false)
    private Integer salaryMale;

    @Column(name = "salary_female", nullable = false)
    private Integer salaryFemale;

    @Column
    private Boolean meal = false;

    @Column
    private Boolean snack = false;

    @Column(name = "transport_allowance")
    private Boolean transportAllowance = false;

    @Column(name = "recruit_count_male")
    private Integer recruitCountMale = 0;

    @Column(name = "recruit_count_female")
    private Integer recruitCountFemale = 0;

    @Column(name = "experience_required")
    private Boolean experienceRequired = false;

    @Column
    private Boolean deleted = false;

}
