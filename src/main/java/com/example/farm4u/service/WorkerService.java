package com.example.farm4u.service;

import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.worker.WorkerRequest;
import com.example.farm4u.entity.Worker;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Character.toUpperCase;

@Service
public class WorkerService {
    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /** 1. 구직자 등록 */
    @Transactional
    public void save(Long userId, WorkerRequest req) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = null;
        if (req.getBirth() != null) {
            birthDate = sdf.parse(req.getBirth());
        }

        Set<Worker.WorkDay> workDaysEnumSet = req.getWorkDays().stream()
                .map(String::toUpperCase)         // 대문자 변환
                .map(Worker.WorkDay::valueOf)     // 문자열 -> enum 변환
                .collect(Collectors.toSet());

        Set<Worker.FarmExpType> farmExpTypeSet = req.getFarmExpTypes().stream()
                .map(String::toUpperCase)         // 대문자 변환
                .map(Worker.FarmExpType::valueOf)     // 문자열 -> enum 변환
                .collect(Collectors.toSet());

        Set<Worker.FarmExpTask> farmExpTaskSet = req.getFarmExpTasks().stream()
                .map(String::toUpperCase)         // 대문자 변환
                .map(Worker.FarmExpTask::valueOf)     // 문자열 -> enum 변환
                .collect(Collectors.toSet());

        Worker.WorkType workTypeEnum = null;
        if (req.getWorkType() != null) {
            workTypeEnum = Worker.WorkType.valueOf(req.getWorkType().toUpperCase());
        }

        Worker worker = Worker.builder()
                .userId(userId)
                .name(req.getName())
                .gender(Worker.Gender.valueOf(req.getGender()))
                .birth(birthDate)
                .address(req.getAddress())
                .activeArea(req.getActiveArea())
                .workType(workTypeEnum)
                .workDays(workDaysEnumSet)
                .hasFarmExp(req.getHasFarmExp())
                .farmExpTypes(farmExpTypeSet)
                .farmExpTasks(farmExpTaskSet)
                .workIntensity(Worker.WorkIntensity.valueOf(req.getWorkIntensity().toUpperCase()))
                .deleted(false)
                .build();
        workerRepository.save(worker);
    }

    /** 2. 내 정보 수정 (deleted = false 조건) */
    @Transactional
    public void update(Long userId, WorkerRequest req) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = null;
        if (req.getBirth() != null) {
            birthDate = sdf.parse(req.getBirth());
        }

        Worker.WorkType workTypeEnum = null;
        if (req.getWorkType() != null) {
            workTypeEnum = Worker.WorkType.valueOf(req.getWorkType().toUpperCase());
        }

        Worker worker = workerRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("구직자 프로필"));
        if (req.getName() != null) worker.setName(req.getName());
        if (req.getGender() != null) worker.setGender(Worker.Gender.valueOf(req.getGender()));
        if (req.getBirth() != null) worker.setBirth(birthDate);
        if (req.getAddress() != null) worker.setAddress(req.getAddress());
        if (req.getActiveArea() != null) worker.setActiveArea(req.getActiveArea());
        if (req.getWorkType() != null) worker.setWorkType(workTypeEnum);
        if (req.getWorkDays() != null) worker.setWorkDays(req.getWorkDays().stream().map(String::toUpperCase).map(Worker.WorkDay::valueOf).collect(Collectors.toSet()));
        if (req.getHasFarmExp() != null) worker.setHasFarmExp(req.getHasFarmExp());
        if (req.getFarmExpTypes() != null) worker.setFarmExpTypes(req.getFarmExpTypes().stream().map(String::toUpperCase).map(Worker.FarmExpType::valueOf).collect(Collectors.toSet()));
        if (req.getFarmExpTasks() != null) worker.setFarmExpTasks(req.getFarmExpTasks().stream().map(String::toUpperCase).map(Worker.FarmExpTask::valueOf).collect(Collectors.toSet()));
        if (req.getWorkIntensity() != null) worker.setWorkIntensity(Worker.WorkIntensity.valueOf(req.getWorkIntensity().toUpperCase()));
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
