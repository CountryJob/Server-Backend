package com.example.farm4u.controller;

import com.example.farm4u.dto.application.ApplicationDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.application.ApplicationCreateRequest;
import com.example.farm4u.entity.Application;
import com.example.farm4u.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    public ApplicationController(ApplicationService applicationService) { this.applicationService = applicationService; }

    /** 1. 지원(자) 상세 조회 (Admin/Farmer만) */
    @GetMapping("/{id}")
    public ResponseEntity<WorkerDto> getApplicationDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        WorkerDto app = applicationService.getApplicationDetail(userId, id);
        return ResponseEntity.ok(app);
    }

    /** 2. 농가→지원자 수락 */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<Void> acceptApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        applicationService.acceptApplication(userId, id);
        return ResponseEntity.ok().build();
    }

    /** 3. 농가→지원자 거절 */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        applicationService.rejectApplication(userId, id);
        return ResponseEntity.ok().build();
    }

    /** 4. 농가→지원자 수락취소 */
    @PatchMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        applicationService.revokeApplication(userId, id);
        return ResponseEntity.ok().build();
    }

    /** 5. 지원하기 (Admin/Worker 본인) */
    @PostMapping
    public ResponseEntity<Void> apply(
            @AuthenticationPrincipal Long userId,
            @RequestBody ApplicationCreateRequest request
    ) {
        applicationService.apply(userId, request);
        return ResponseEntity.ok().build();
    }

    /** 6. 내 지원내역 전체/필터 조회 (Admin/Worker만) */
    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getMyApplications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "status", required = false) String status // 필터
    ) {
        List<ApplicationDto> list = applicationService.getMyApplications(userId, Application.ApplyStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(list);
    }

    /** 7. 내 지원 취소 (Admin/Worker 본인) */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        applicationService.cancelApplication(userId, id);
        return ResponseEntity.ok().build();
    }
}
