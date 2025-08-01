package com.example.farm4u.repository;

import com.example.farm4u.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 내 알림 전체 리스트(읽음/안읽음/최신순, 삭제 제외) */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.deleted = false ORDER BY n.createdAt DESC")
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    /** 단일 알림(권한) */
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.userId = :userId AND n.deleted = false")
    Notification findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /** 단일 알림 읽음처리 */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.userId = :userId")
    int markRead(@Param("userId") Long userId, @Param("id") Long id);

    /** 전체 알림 읽음 처리 */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.deleted = false")
    int markAllRead(@Param("userId") Long userId);
}
