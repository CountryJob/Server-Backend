package com.example.farm4u.controller;

import com.example.farm4u.dto.ai.AutoRateReviewRequest;
import com.example.farm4u.dto.ai.AutoWriteJobRequest;
import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }
    
//    1, 2번 둘 다 JobController에서 다룸
//
//    /**
//     * 1. 구직자(Job) 대상 추천
//     * - 워커 입장에서 맞춤 일자리 추천
//     */
//    @PostMapping("/recommend/jobs")
//    public ResponseEntity<List<JobDto>> recommendJobs(@AuthenticationPrincipal Long userId) {
//        List<JobDto> jobs = aiService.recommendJobsForWorker(userId);
//        return ResponseEntity.ok(jobs);
//    }
//
//    /**
//     * 2. 농가 기준 워커 추천
//     * - 농장주 입장에서 적합한 구직자 추천 -> X
//     * -> 공고별로 적합한 구직자 추천 리스트 리턴
//     */
//    @PostMapping("/recommend/workers/{jobId}")
//    public ResponseEntity<List<WorkerDto>> recommendWorkers(@AuthenticationPrincipal Long userId, @PathVariable Long jobId) {
//        List<WorkerDto> workers = aiService.recommendWorkersForFarmer(userId, jobId);
//        return ResponseEntity.ok(workers);
//    }

    /**
     * 3. 공고 자동 작성
     * - AI가 농가/상황 기반으로 자동 문구, 조건 등 제안
     * - job쪽에서 job create 요청 시 한번에 처리
     */
//    @PostMapping("/auto-write/job")
//    public ResponseEntity<JobDto> autoWriteJob(
//            @AuthenticationPrincipal Long userId,
//            @RequestParam("type") String type,                // 요청받은 문자열 파라미터
//            @RequestParam("audio") MultipartFile audioFile
//    ) {
//        JobDto posting = aiService.autoWriteJob(userId, type, audioFile);
//
//        return ResponseEntity.ok(posting);
//    }

    /**
     * 4. 후기 기반 신뢰 점수 업데이트 (비동기/백그라운드 트리거)
     * - 대상 타입(targetType) 명시: worker/farmer 중 하나
     * - reviewId: 해당 리뷰 primary key
     * - targetId: 점수 갱신 대상의 user_id
     */
    @PostMapping("/auto-rate/review")
    public ResponseEntity<Void> autoRateReview(@RequestBody AutoRateReviewRequest req) {
        aiService.updateAiScoreAsync(req.getTargetId(), req.getReviewId(), req.getTargetType());
        return ResponseEntity.accepted().build(); // 202 Accepted: 비동기 요청
    }
}
