package com.example.farm4u.controller;

import com.example.farm4u.dto.review.ReviewDto;
import com.example.farm4u.dto.review.ReviewRequest;
import com.example.farm4u.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) { this.reviewService = reviewService; }

    /** 후기 등록 (userId만 받음, userMode는 Service내에서 자동 조회/분기) */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @AuthenticationPrincipal Long userId,
            @RequestBody ReviewRequest req
    ) {
        return ResponseEntity.ok(reviewService.createReview(userId, req));
    }

    /** 후기 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody ReviewRequest req
    ) {
        return ResponseEntity.ok(reviewService.updateReview(userId, id, req));
    }

    /** 후기 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal Long userId, @PathVariable Long id
    ) {
        reviewService.deleteReview(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** 후기 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewDetail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewDetail(id));
    }

    /** farmer에 달린 리뷰 리스트 : worker->(jobs)farmer 후기 리스트 조회 */
    @GetMapping("/farmer/{farmerUserId}")
    public ResponseEntity<List<ReviewDto>> getFarmerReviewList(@PathVariable Long farmerUserId) {
        return ResponseEntity.ok(reviewService.getWorkerToFarmerReviews(farmerUserId));
    }

    /** worker에 달린 리뷰 리스트 조회 : farmer -> worker */
    @GetMapping("/worker/{workerUserId}")
    public ResponseEntity<List<ReviewDto>> getWorkerReviewList(
            @PathVariable Long workerUserId
    ) {
        return ResponseEntity.ok(reviewService.getFarmerToWorkerReviews(workerUserId));
    }
}

