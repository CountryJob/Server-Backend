package com.example.farm4u.controller;

import com.example.farm4u.dto.notification.NotificationDto;
import com.example.farm4u.dto.notification.NotificationCreateRequest;
import com.example.farm4u.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    /** 알림 리스트 조회 */
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationList(userId));
    }

    /** 알림 읽음 처리 */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        notificationService.markNotificationRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** 알림 전체 읽음 처리 */
    @PostMapping("/read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    /** 알림 생성 (내부 시스템 용) */
    @PostMapping("/internal")
    public ResponseEntity<NotificationDto> createInternal(@RequestBody NotificationCreateRequest req) {
        return ResponseEntity.ok(notificationService.createNotificationInternal(req));
    }
}
