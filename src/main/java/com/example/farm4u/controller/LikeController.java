package com.example.farm4u.controller;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.like.LikeDto;
import com.example.farm4u.dto.like.LikeCreateRequest;
import com.example.farm4u.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;
    public LikeController(LikeService likeService) { this.likeService = likeService; }

    /** 관심 등록 */
    @PostMapping
    public ResponseEntity<LikeDto> createLike(@AuthenticationPrincipal Long userId, @RequestBody LikeCreateRequest req) {
        return ResponseEntity.ok(likeService.createLike(userId, req));
    }

    /** 관심 해제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLike(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        likeService.deleteLike(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** 관심 공고 리스트 조회 */
    @GetMapping("/list")
    public ResponseEntity<List<JobDto>> getLikeList(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(likeService.getLikedJobsForUser(userId));
    }
}
