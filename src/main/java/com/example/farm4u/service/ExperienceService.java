package com.example.farm4u.service;

import com.example.farm4u.dto.experience.ExperienceDto;
import com.example.farm4u.entity.Experience;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.ExperienceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    // 1. 내 작업이력 전체 조회 (본인)
    public List<ExperienceDto> getExperiencesForUser(Long userId) {
        List<Experience> list = experienceRepository.findAllByUserId(userId);
        return list.stream().map(ExperienceDto::new).collect(Collectors.toList());
    }

    // 2. 내 작업이력 상세 조회 (본인)
    public ExperienceDto getExperienceDetail(Long userId, Long experienceId) {
        Experience exp = experienceRepository.findByIdAndUserId(experienceId, userId)
                .orElseThrow(() -> new NotFoundException("해당 작업이력"));
        return new ExperienceDto(exp);
    }
}
