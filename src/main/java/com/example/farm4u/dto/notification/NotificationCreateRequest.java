package com.example.farm4u.dto.notification;

import com.example.farm4u.entity.Notification;
import lombok.*;

@Getter @Setter
public class NotificationCreateRequest {
    private Long userId;           // 알림 수신자 id (내부 시스템 발송용)
    private Notification.NotifyType notifyType;     // 알림 유형
    private String content;        // 알림 메시지 내용
}
