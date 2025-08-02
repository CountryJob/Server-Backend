package com.example.farm4u.dto.worker;

import com.example.farm4u.entity.Worker;
import lombok.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 구직자 API 응답용 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerDto {

    private Long userId;                 // workers.user_id
    private String name;
    private String gender;               // "MALE" or "FEMALE"
    private String birth;                // YYYY-MM-DD
    private String address;
    private Double latitude;
    private Double longitude;
    private String activeArea;

    private String workType;             // "SHORT", "LONG", "ANY"
    private List<String> workDays;       // ["MON","TUE",...]
    private Boolean hasFarmExp;

    private List<String> farmExpTypes;   // ["FRUIT", ...]
    private List<String> farmExpTasks;   // ["GROW", ...]
    private String workIntensity;        // "LIGHT","MEDIUM","ACTIVE"

    private Double avgSincerityRating;
    private Double avgPromiseRating;
    private Double avgSkillRating;
    private Double avgRehireRating;
    private Integer reviewCount;
    private Double trustScore;

    private String createdAt;            // YYYY-MM-DD HH:mm:ss
    private String updatedAt;
    private Boolean deleted;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WorkerDto(Worker worker) {

        this.userId = worker.getUserId();
        this.name = worker.getName();
        this.gender = (worker.getGender() != null) ? worker.getGender().name() : null;
        this.birth = (worker.getBirth() != null)
                ? new SimpleDateFormat("yyyy-MM-dd").format(worker.getBirth()) : null;
        this.address = worker.getAddress();
        this.latitude = (worker.getLatitude() != null) ? worker.getLatitude().doubleValue() : null;
        this.longitude = (worker.getLongitude() != null) ? worker.getLongitude().doubleValue() : null;
        this.activeArea = worker.getActiveArea();

        this.workType = (worker.getWorkType() != null) ? worker.getWorkType().name() : null;
        this.workDays = (worker.getWorkDays() != null)
                ? worker.getWorkDays().stream().map(Enum::name).collect(Collectors.toList()) : Collections.emptyList();
        this.hasFarmExp = worker.getHasFarmExp();

        this.farmExpTypes = (worker.getFarmExpTypes() != null)
                ? worker.getFarmExpTypes().stream().map(Enum::name).collect(Collectors.toList()) : Collections.emptyList();
        this.farmExpTasks = (worker.getFarmExpTasks() != null)
                ? worker.getFarmExpTasks().stream().map(Enum::name).collect(Collectors.toList()) : Collections.emptyList();

        this.workIntensity = (worker.getWorkIntensity() != null) ? worker.getWorkIntensity().name() : null;

        this.avgSincerityRating = (worker.getAvgSincerityRating() != null) ? worker.getAvgSincerityRating().doubleValue() : null;
        this.avgPromiseRating = (worker.getAvgPromiseRating() != null) ? worker.getAvgPromiseRating().doubleValue() : null;
        this.avgSkillRating = (worker.getAvgSkillRating() != null) ? worker.getAvgSkillRating().doubleValue() : null;
        this.avgRehireRating = (worker.getAvgRehireRating() != null) ? worker.getAvgRehireRating().doubleValue() : null;

        this.reviewCount = worker.getReviewCount();
        this.trustScore = (worker.getTrustScore()!=null)? worker.getTrustScore() : null;
        this.createdAt = (worker.getCreatedAt() != null)
                ? worker.getCreatedAt().format(formatter)
                : null;
        this.updatedAt = (worker.getUpdatedAt() != null)
                ? worker.getUpdatedAt().format(formatter)
                : null;
        this.deleted = worker.getDeleted();
    }
}
