package com.example.farm4u.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "notifications") // TODO: indexes
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드만
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notify_type", nullable = false)
    private NotifyType notifyType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    public enum NotifyType{
        APPLIED, SUGGESTED, MATCHED, JOB_CLOSED, REVIEW_REQUEST, ETC
    }

    @PrePersist
    protected void init(){
        if (isRead == null) isRead = false;
        if (deleted == null) deleted = false;
    }
}


