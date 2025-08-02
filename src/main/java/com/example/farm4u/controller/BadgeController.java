package com.example.farm4u.controller;

import com.example.farm4u.dto.badge.BadgeDto;
import com.example.farm4u.dto.badge.UserBadgeDto;
import com.example.farm4u.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BadgeController {

    private final BadgeService badgeService;
    public BadgeController(BadgeService badgeService) { this.badgeService = badgeService; }

    /** 전체 뱃지 리스트 */
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeDto>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    /** 특정 뱃지 상세 */
    @GetMapping("/badges/{id}")
    public ResponseEntity<BadgeDto> getBadgeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(badgeService.getBadgeDetail(id));
    }

    /** 특정 유저(본인)의 뱃지 리스트 */
    @GetMapping("/users/badges")
    public ResponseEntity<List<UserBadgeDto>> getUserBadges(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }
}
