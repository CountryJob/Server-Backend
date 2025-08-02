package com.example.farm4u.controller;

import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.worker.WorkerRequest;
import com.example.farm4u.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/workers")
public class WorkerController {
    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    /** 1. 구직자(나) 등록 */
    @PostMapping
    public ResponseEntity<Void> save(
            @AuthenticationPrincipal Long userId,
            @RequestBody WorkerRequest req) throws ParseException {
        workerService.save(userId, req);
        return ResponseEntity.ok().build();
    }

    /** 2. 나의 정보 수정 */
    @PatchMapping
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Long userId,
            @RequestBody WorkerRequest req) throws ParseException {
        workerService.update(userId, req);
        return ResponseEntity.ok().build();
    }

    /** 3. 나의 정보 조회 */
    @GetMapping
    public ResponseEntity<WorkerDto> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(workerService.findById(userId));
    }

    /** 4. 특정 구직자 정보 조회(관리자) */
    @GetMapping("/{id}")
    public ResponseEntity<WorkerDto> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(workerService.findByAdmin(id));
    }

    /** 5. 전체 구직자 리스트 조회(관리자) */
    @GetMapping("/list")
    public ResponseEntity<List<WorkerDto>> getAll() {
        return ResponseEntity.ok(workerService.findAll());
    }
}
