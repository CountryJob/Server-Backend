package com.example.farm4u.service;

import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.worker.WorkerRequest;
import com.example.farm4u.entity.Worker;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerService {
    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /** 1. 구직자 등록 */
    @Transactional
    public void save(Long userId, WorkerRequest req) {
        Worker worker = Worker.builder()
                .userId(userId)
                .name(req.getName())
                .gender(req.getGender())
                .birth(req.getBirth())
                .address(req.getAddress())
                .activeArea(req.getActiveArea())
                .workType(req.getWorkType())
                .workDays(req.getWorkDays())
                .hasFarmExp(req.getHasFarmExp())
                .farmExpTypes(req.getFarmExpTypes())
                .farmExpTasks(req.getFarmExpTasks())
                .workIntensity(req.getWorkIntensity())
                .deleted(false)
                .build();
        workerRepository.save(worker);
    }

    /** 2. 내 정보 수정 (deleted = false 조건) */
    @Transactional
    public void update(Long userId, WorkerRequest req) {
        Worker worker = workerRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("구직자 프로필"));
        if (req.getName() != null) worker.setName(req.getName());
        if (req.getGender() != null) worker.setGender(req.getGender());
        if (req.getBirth() != null) worker.setBirth(req.getBirth());
        if (req.getAddress() != null) worker.setAddress(req.getAddress());
        if (req.getActiveArea() != null) worker.setActiveArea(req.getActiveArea());
        if (req.getWorkType() != null) worker.setWorkType(req.getWorkType());
        if (req.getWorkDays() != null) worker.setWorkDays(req.getWorkDays());
        if (req.getHasFarmExp() != null) worker.setHasFarmExp(req.getHasFarmExp());
        if (req.getFarmExpTypes() != null) worker.setFarmExpTypes(req.getFarmExpTypes());
        if (req.getFarmExpTasks() != null) worker.setFarmExpTasks(req.getFarmExpTasks());
        if (req.getWorkIntensity() != null) worker.setWorkIntensity(req.getWorkIntensity());
    }

    /** 3. 내 정보 조회 */
    public WorkerDto findById(Long userId) {
        Worker worker = workerRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("구직자 정보"));
        return new WorkerDto(worker);
    }

    /** 4. 특정 구직자 정보 조회(관리자) */
    public WorkerDto findByAdmin(Long id) {
        Worker worker = workerRepository.findByUserIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("구직자 정보"));
        return new WorkerDto(worker);
    }

    /** 5. 전체 구직자 리스트(관리자) */
    public List<WorkerDto> findAll() {
        return workerRepository.findAllByDeletedFalse()
                .stream()
                .map(WorkerDto::new)
                .collect(Collectors.toList());
    }
}
