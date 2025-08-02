package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;

@AllArgsConstructor @Builder
@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "workers") // TODO: indexes
public class Worker extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Date birth;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;                    // address 입력 받을 때 자동 업데이트? - 비즈니스 레벨

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;                   //

    @Column(name = "active_area", length = 255)     //
    private String activeArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type")
    private WorkType workType;

    // work_days(SET) -> JPA의 컬렉션 매핑
    // : worker_work_days(테이블) + work_day(컬럼) 조합
    // user_id : work_day 다대다 형식
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "worker_work_days", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "work_day")
    @Enumerated(EnumType.STRING)
    private Set<WorkDay> workDays;

    @Column(name = "has_farm_exp")
    private Boolean hasFarmExp;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "worker_farm_exp_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "farm_exp_type")
    @Enumerated(EnumType.STRING)
    private Set<FarmExpType> farmExpTypes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable( name = "worker_farm_exp_tasks", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "farm_exp_task")
    @Enumerated(EnumType.STRING)
    private Set<FarmExpTask> farmExpTasks;

    @Column(name = "work_intensity")
    @Enumerated(EnumType.STRING)
    private WorkIntensity workIntensity;

    @Column(name = "avg_sincerity_rating", precision = 3, scale = 2)
    private BigDecimal avgSincerityRating;

    @Column(name = "avg_promise_rating", precision = 3, scale = 2)
    private BigDecimal avgPromiseRating;

    @Column(name = "avg_skill_rating", precision = 3, scale = 2)
    private BigDecimal avgSkillRating;

    @Column(name = "avg_rehire_rating", precision = 3, scale = 2)
    private BigDecimal avgRehireRating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "ai_score", columnDefinition = "DOUBLE CHECK(ai_score BETWEEN 0 AND 100)")
    private Double aiScore;

    @Column(nullable = false)
    private Boolean deleted = false;

    public enum Gender{ MALE, FEMALE }
    public enum WorkType{ SHORT, LONG, ANY }
    public enum WorkDay { MON, TUE, WED, THU, FRI, SAT, SUN}
    public enum FarmExpType { FRUIT, VEGETABLE, GRAIN, SPECIAL, FLOWER, TREE, ETC }
    public enum FarmExpTask { GROW, MANAGE, HARVEST, PACK, ETC }
    public enum WorkIntensity{ LIGHT, MEDIUM, ACTIVE }

    @PrePersist
    protected void init(){
        if (reviewCount == null) reviewCount = 0;
        if (reportCount == null) reportCount = 0;
        if (deleted == null) deleted = false;
        StringTokenizer st = new StringTokenizer(address);
        activeArea = st.nextToken();
    }
}
