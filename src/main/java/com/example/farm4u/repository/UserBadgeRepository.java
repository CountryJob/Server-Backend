package com.example.farm4u.repository;

import com.example.farm4u.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId AND ub.deleted = false")
    List<UserBadge> findAllByUserId(Long userId);

    // 같은 뱃지 중복 지급 방지용
    Optional<UserBadge> findByUser_IdAndBadge_IdAndDeletedFalse(Long userId, Long badgeId);
}
