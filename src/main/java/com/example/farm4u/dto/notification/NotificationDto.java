package com.example.farm4u.dto.notification;

import com.example.farm4u.entity.Notification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDto {
    private Long id;           // notifications.id
    private Long userId;       // 수신자
    private String notifyType; // Enum String ('APPLIED', 'SUGGESTED', 'MATCHED', 'JOB_CLOSED', 'REVIEW_REQUEST', 'ETC')
    private String content;    // 알림 내용
    private Boolean isRead;    // 읽음여부
    private String createdAt;  // 생성일

    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUserId();
        this.notifyType = notification.getNotifyType() != null ? notification.getNotifyType().name() : null;
        this.content = notification.getContent();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null;
    }
}
