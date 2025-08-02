package com.example.farm4u.controller;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.service.AiService;
import com.example.farm4u.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 공고 API 컨트롤러
 * - 등록, 수정, 삭제, 마감, 조건/정렬별 공고 리스트, 상세, 지원자(AI점수기준) 모두 지원!
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final AiService aiService;

    public JobController(JobService jobService, AiService aiService) {
        this.jobService = jobService;
        this.aiService = aiService;
    }

    /** 1. 공고 등록 (본인(농장주)) */
    // 1-1. 파일로 전달받아 자동작성해 리턴
    @PostMapping("/auto-write")
    public ResponseEntity<JobDto> autoWriteJob(
            @AuthenticationPrincipal Long userId,
            @RequestPart MultipartFile audioFile
    ) throws IOException {
        System.out.println("JobController: autoWriteJob(): userId = " + userId);
        JobDto created = jobService.autoWriteAndCreateJob(userId, audioFile);
        return ResponseEntity.ok(created);
    }

    // 1-2. 수정 및 확인 후 최종 공고 등록
    @PostMapping
    public ResponseEntity<Void> createJob(
            @AuthenticationPrincipal Long userId,
            @RequestBody JobDto jobDto){
        System.out.println("JobController: createJob(): userId = " + userId);
        jobService.createJob(userId, jobDto);
        return ResponseEntity.ok().build();
    }

    // 1) 질문-답변 당 AI 필드 변환
//    @PostMapping("/auto-write/field")
//    public ResponseEntity<Map<String, String>> autoWriteField(
//            @RequestParam String questionKey,
//            @RequestPart MultipartFile audioFile
//    ) throws IOException {
//        String transcribed = jobService.autoWriteField(questionKey, audioFile);
//        Map<String, String> result = Map.of(
//                "key", questionKey,
//                "transcribed", transcribed
//        );
//
//        // 프론트에서 각 질문별로 해당 API 호출 후 결과(key, transcribed)로 저장
//        return ResponseEntity.ok(result);
//    }
    
    // 2) 전체 필드에 대해 자동작성 후 저장
//    @PostMapping
//    public ResponseEntity<JobDto> createJob(
//            @AuthenticationPrincipal Long userId,
//            @RequestBody JobRequest request // -> 음성파일을 받도록 수정하기
//    ) {
//        JobDto created = jobService.createJob(userId, request);
//        return ResponseEntity.ok(created);
//    }

    /** 2. 공고 수정 (본인) */
    @PutMapping("/{id}")
    public ResponseEntity<JobDto> updateJob(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody JobRequest request
    ) {
        JobDto updated = jobService.updateJob(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    /** 3. 공고 삭제 (논리삭제, 본인/관리자만) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @AuthenticationPrincipal Long userId, @PathVariable Long id
    ) {
        jobService.deleteJob(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** 4. 공고 마감 (본인/관리자만) */
    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeJob(
            @AuthenticationPrincipal Long userId, @PathVariable Long id
    ) {
        jobService.closeJob(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 5. 공고 리스트 조회
     * - 모든 필터/정렬(거리, 경력, 임금, 날짜, 마감, 정렬옵션) 및 권한별(userMode)에 따라 분기 처리
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getJobList(
            @AuthenticationPrincipal Long userId,
//            @RequestParam(value = "lat", required = false) Double lat,
//            @RequestParam(value = "lng", required = false) Double lng,
//            @RequestParam(value = "radius", required = false) Double radius,
//            @RequestParam(value = "experienceRequired", required = false) Boolean experienceRequired,
//            @RequestParam(value = "salaryMaleMin", required = false) Integer salaryMaleMin,
//            @RequestParam(value = "salaryFemaleMin", required = false) Integer salaryFemaleMin,
//            @RequestParam(value = "startDateFrom", required = false) LocalDate startDateFrom,
//            @RequestParam(value = "startDateTo", required = false) LocalDate startDateTo,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        System.out.println("job controller getJobList: userId: "+userId);
        List<JobDto> jobs = jobService.getJobList(userId,
//                lat, lng, radius,
//                experienceRequired, salaryMaleMin, salaryFemaleMin, startDateFrom, startDateTo,
                sort);
        return ResponseEntity.ok(jobs);
    }

    /** 6. 특정 공고 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(@PathVariable Long id) {
        JobDto jobDto = jobService.getJobById(id);
        return ResponseEntity.ok(jobDto);
    }

    /**
     * 7. 특정 공고 지원자 리스트 (AI 매칭점수 높은 순으로 정렬)
     * - farmer 모드(본인 공고)에 한해 사용
     */
    @GetMapping("/{id}/applicants")
    public ResponseEntity<List<WorkerDto>> getApplicantsWithAiScore(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        List<WorkerDto> applicants = jobService.getApplicantsForJobWithAiScore(userId, id);
        return ResponseEntity.ok(applicants);
    }
}
