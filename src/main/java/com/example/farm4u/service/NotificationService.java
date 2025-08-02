package com.example.farm4u.service;

import com.example.farm4u.dto.notification.NotificationDto;
import com.example.farm4u.dto.notification.NotificationCreateRequest;
import com.example.farm4u.entity.Notification;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** 알림 리스트 조회(본인) */
    public List<NotificationDto> getNotificationList(Long userId) {
        return notificationRepository.findAllByUserId(userId)
                .stream().map(NotificationDto::new).collect(Collectors.toList());
    }

    /** 단일 알림 읽음 처리 */
    @Transactional
    public void markNotificationRead(Long userId, Long notificationId) {
        int updated = notificationRepository.markRead(userId, notificationId);
        if (updated == 0) throw new NotFoundException("알림 (또는 권한)");
    }

    /** 전체 읽음 처리 */
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    /** 시스템 내부용 알림 생성 */
    @Transactional
    public NotificationDto createNotificationInternal(NotificationCreateRequest req) {
        Notification n = Notification.builder()
                .userId(req.getUserId())
                .notifyType(req.getNotifyType())
                .content(req.getContent())
                .isRead(false)
                .deleted(false)
                .build();
        Notification saved = notificationRepository.save(n);
        return new NotificationDto(saved);
    }
}
