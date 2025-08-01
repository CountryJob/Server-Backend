package com.example.farm4u.controller;

import com.example.farm4u.dto.experience.ExperienceDto;
import com.example.farm4u.service.ExperienceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /** 1. 본인 작업이력 전체 조회 */
    @GetMapping
    public ResponseEntity<List<ExperienceDto>> getMyExperiences(@AuthenticationPrincipal Long userId) {
        List<ExperienceDto> list = experienceService.getExperiencesForUser(userId);
        return ResponseEntity.ok(list);
    }

    /** 2. 본인 작업이력 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceDto> getExperienceDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        ExperienceDto detail = experienceService.getExperienceDetail(userId, id);
        return ResponseEntity.ok(detail);
    }
}
