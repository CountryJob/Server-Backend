package com.example.farm4u.dto.application;

import com.example.farm4u.entity.Application;
import lombok.*;

import java.text.SimpleDateFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto {

    private Long id;                // applications.id
    private Long userId;            // worker.userId
    private Long jobId;             // job.id
    private String applyStatus;     // "APPLIED", "MATCHED", ...
    private Integer priority;
    private String createdAt;       // YYYY-MM-DD HH:mm:ss
    private String updatedAt;
    private Boolean deleted;

    public ApplicationDto(Application app) {
        this.id = app.getId();
        this.userId = (app.getWorker() != null) ? app.getWorker().getUserId() : null;
        this.jobId = (app.getJob() != null) ? app.getJob().getId() : null;
        this.applyStatus = (app.getApplyStatus() != null) ? app.getApplyStatus().name() : null;
        this.priority = app.getPriority();
        this.createdAt = (app.getCreatedAt() != null)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(app.getCreatedAt()) : null;
        this.updatedAt = (app.getUpdatedAt() != null)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(app.getUpdatedAt()) : null;
        this.deleted = app.getDeleted();
    }
}
