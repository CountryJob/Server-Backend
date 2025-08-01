package com.example.farm4u.controller;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 공고 API 컨트롤러
 * - 등록, 수정, 삭제, 마감, 조건/정렬별 공고 리스트, 상세, 지원자(AI점수기준) 모두 지원!
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** 1. 공고 등록 (관리자/농가만) */
    @PostMapping
    public ResponseEntity<JobDto> createJob(
            @AuthenticationPrincipal Long userId,
            @RequestBody JobRequest request
    ) {
        JobDto created = jobService.createJob(userId, request);
        return ResponseEntity.ok(created);
    }

    /** 2. 공고 수정 (본인/관리자만) */
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
            @RequestParam(value = "userMode", required = false, defaultValue = "ANONYMOUS") String userMode,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "radius", required = false) Double radius,
            @RequestParam(value = "experienceRequired", required = false) Boolean experienceRequired,
            @RequestParam(value = "salaryMaleMin", required = false) Integer salaryMaleMin,
            @RequestParam(value = "salaryFemaleMin", required = false) Integer salaryFemaleMin,
            @RequestParam(value = "startDateFrom", required = false) LocalDate startDateFrom,
            @RequestParam(value = "startDateTo", required = false) LocalDate startDateTo,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        List<JobDto> jobs = jobService.getJobList(
                userMode, lat, lng, radius,
                experienceRequired, salaryMaleMin, salaryFemaleMin,
                startDateFrom, startDateTo, sort
        );
        return ResponseEntity.ok(jobs);
    }

    /** 6. 특정 공고 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(@PathVariable Long id) {
        JobDto job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
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
