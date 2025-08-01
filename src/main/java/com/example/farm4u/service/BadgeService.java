package com.example.farm4u.service;

import com.example.farm4u.dto.badge.BadgeDto;
import com.example.farm4u.dto.badge.UserBadgeDto;
import com.example.farm4u.entity.Badge;
import com.example.farm4u.entity.User;
import com.example.farm4u.entity.UserBadge;
import com.example.farm4u.repository.BadgeRepository;
import com.example.farm4u.repository.UserBadgeRepository;
import com.example.farm4u.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        UserBadgeRepository userBadgeRepository,
                        UserRepository userRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
    }

    public List<BadgeDto> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(b -> new BadgeDto(
                        b.getId(), b.getTitle(), b.getDescription(), b.getImgUrl(),
                        b.getBadgeType().name(), b.getBadgeCondition()
                )).collect(Collectors.toList());
    }

    public BadgeDto getBadgeDetail(Long id) {
        Badge b = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 뱃지"));
        return new BadgeDto(
                b.getId(), b.getTitle(), b.getDescription(), b.getImgUrl(),
                b.getBadgeType().name(), b.getBadgeCondition()
        );
    }

    /** 내 뱃지 리스트 */
    public List<UserBadgeDto> getUserBadges(Long userId) {
        List<UserBadge> list = userBadgeRepository.findAllByUserId(userId);
        return list.stream().map(ub -> {
            Badge b = ub.getBadge();
            return new UserBadgeDto(
                    ub.getId(),
                    b.getId(),
                    b.getTitle(),
                    b.getDescription(),
                    b.getImgUrl(),
                    b.getBadgeType().name(),
                    b.getBadgeCondition(),
                    ub.getCreatedAt() != null ? ub.getCreatedAt().toString() : null
            );
        }).collect(Collectors.toList());
    }

    /** 내부용 자동 뱃지 지급: 조건 확인 후 지급, 중복지급 방지 */
    @Transactional
    public void grantUserBadge(Long userId, Long badgeId) {
        userBadgeRepository.findByUser_IdAndBadge_IdAndDeletedFalse(userId, badgeId)
                .ifPresent(ub -> { throw new IllegalStateException("이미 지급된 뱃지"); });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("잘못된 사용자"));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("잘못된 뱃지"));

        UserBadge newBadge = UserBadge.builder().user(user).badge(badge).deleted(false).build();
        userBadgeRepository.save(newBadge);
    }
}
