package com.example.farm4u.service;

import com.example.farm4u.AiClient;
import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.entity.Application;
import com.example.farm4u.entity.Farmer;
import com.example.farm4u.entity.Job;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.ApplicationRepository;
import com.example.farm4u.repository.FarmerRepository;
import com.example.farm4u.repository.JobRepository;
import com.example.farm4u.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JobService
 * - 공고 등록 시 농가정보(address/areaSize 등) 자동 반영
 * - 각종 필터, 거리, 정렬 조건 완벽 분기 처리
 * - 지원자 리스트 AI Score 정렬까지!
 */
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final FarmerRepository farmerRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkerRepository workerRepository;
    private final AiClient aiClient; // AI 매칭 점수용 외부 클라이언트(의존성 주입)

    public JobService(
            JobRepository jobRepository,
            FarmerRepository farmerRepository,
            ApplicationRepository applicationRepository,
            WorkerRepository workerRepository,
            AiClient aiClient
    ) {
        this.jobRepository = jobRepository;
        this.farmerRepository = farmerRepository;
        this.applicationRepository = applicationRepository;
        this.workerRepository = workerRepository;
        this.aiClient = aiClient;
    }

    /** 1. 공고 등록(농가 기준 address, area사이즈 자동 세팅) */
    @Transactional
    public JobDto createJob(Long userId, JobRequest req) {
        Farmer farmer = farmerRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("농가 계정 정보 없음"));
        Job newJob = Job.builder()
                .userId(userId)
                .title(req.getTitle())
                .description(req.getDescription())
                .address(farmer.getAddress())
                .areaSize(farmer.getAreaSize())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .closed(false)
                .salaryMale(req.getSalaryMale())
                .salaryFemale(req.getSalaryFemale())
                .meal(req.getMeal())
                .snack(req.getSnack())
                .transportAllowance(req.getTransportAllowance())
                .recruitCountMale(req.getRecruitCountMale() != null ? req.getRecruitCountMale() : 0)
                .recruitCountFemale(req.getRecruitCountFemale() != null ? req.getRecruitCountFemale() : 0)
                .experienceRequired(req.getExperienceRequired() != null ? req.getExperienceRequired() : false)
                .deleted(false)
                .build();
        Job saved = jobRepository.save(newJob);
        return new JobDto(saved);
    }

    /** 2. 공고 수정(본인/관리자 Only, req값만 patch) */
    @Transactional
    public JobDto updateJob(Long userId, Long jobId, JobRequest req) {
        Job job = jobRepository.findByIdAndUserIdAndDeletedFalse(jobId, userId)
                .orElseThrow(() -> new NotFoundException("공고 정보 없음"));
        if (req.getTitle() != null) job.setTitle(req.getTitle());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getStartDate() != null) job.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) job.setEndDate(req.getEndDate());
        if (req.getStartTime() != null) job.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) job.setEndTime(req.getEndTime());
        if (req.getSalaryMale() != null) job.setSalaryMale(req.getSalaryMale());
        if (req.getSalaryFemale() != null) job.setSalaryFemale(req.getSalaryFemale());
        if (req.getMeal() != null) job.setMeal(req.getMeal());
        if (req.getSnack() != null) job.setSnack(req.getSnack());
        if (req.getTransportAllowance() != null) job.setTransportAllowance(req.getTransportAllowance());
        if (req.getRecruitCountMale() != null) job.setRecruitCountMale(req.getRecruitCountMale());
        if (req.getRecruitCountFemale() != null) job.setRecruitCountFemale(req.getRecruitCountFemale());
        if (req.getExperienceRequired() != null) job.setExperienceRequired(req.getExperienceRequired());
        // address/area_size는 농가 정보가 바뀌어야만 반영하므로 수정X
        return new JobDto(job);
    }

    /** 3. 공고 삭제 (논리) */
    @Transactional
    public void deleteJob(Long userId, Long jobId) {
        Job job = jobRepository.findByIdAndUserIdAndDeletedFalse(jobId, userId)
                .orElseThrow(() -> new NotFoundException("공고 정보 없음"));
        job.setDeleted(true);
    }

    /** 4. 공고 마감 */
    @Transactional
    public void closeJob(Long userId, Long jobId) {
        Job job = jobRepository.findByIdAndUserIdAndDeletedFalse(jobId, userId)
                .orElseThrow(() -> new NotFoundException("공고 정보 없음"));
        job.setClosed(true);
    }

    /**
     * 5. 공고 리스트 필터/정렬 다중 분기(거리 필터 사용시 farmer 테이블 조인)
     * userMode: "ANONYMOUS", "WORKER" 등
     */
    public List<JobDto> getJobList(
            String userMode,
            Double lat,
            Double lng,
            Double radius,
            Boolean experienceRequired,
            Integer salaryMaleMin,
            Integer salaryFemaleMin,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            String sort
    ) {
        List<Job> jobs;
        if (lat != null && lng != null && radius != null) {
            jobs = jobRepository.findByAllFiltersAndDistance(
                    lat, lng, radius,
                    experienceRequired,
                    salaryMaleMin,
                    salaryFemaleMin,
                    startDateFrom,
                    startDateTo
            );
            // 결과는 거리순
        } else {
            jobs = jobRepository.findFilteredJobs(
                    experienceRequired,
                    salaryMaleMin,
                    salaryFemaleMin,
                    startDateFrom,
                    startDateTo
            );
            if (sort != null) {
                if ("salary".equalsIgnoreCase(sort)) {
                    jobs.sort(Comparator.comparing(Job::getSalaryMale, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(Job::getSalaryFemale, Comparator.nullsLast(Comparator.reverseOrder())));
                } else if ("period".equalsIgnoreCase(sort)) {
                    jobs.sort(Comparator.comparing(Job::getStartDate, Comparator.nullsFirst(Comparator.naturalOrder())));
                } else if ("closed".equalsIgnoreCase(sort)) {
                    jobs.sort(Comparator.comparing(Job::getClosed, Comparator.nullsLast(Comparator.naturalOrder())));
                }
            } else {
                if ("WORKER".equalsIgnoreCase(userMode)) {
                    // AI 추천점수 API/서비스 연동, 없음: 그대로
                    // 이 기능은 JobDto에 aiScore 등 추가해서 추후 연동 예정!
                } else {
                    // ANONYMOUS, FARMER 등: 최신순(createdAt DESC, JPQL 기본)
                    jobs.sort(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                }
            }
        }

        return jobs.stream().map(JobDto::new).collect(Collectors.toList());
    }

    /** 6. 공고 상세 (삭제된 경우 제외) */
    public JobDto getJobById(Long id) {
        Job job = jobRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("공고 정보 없음"));
        return new JobDto(job);
    }

    /**
     * 7. 특정 공고 지원자 리스트(농가 자기 공고만, AI 매칭 점수순 정렬까지)
     * 지원자마다 trustScore(WorkerDto), AI점수(aiScore) 모두 설정하여 리턴
     */
    public List<WorkerDto> getApplicantsForJobWithAiScore(Long farmerUserId, Long jobId) {
        // 본인 소유 공고인지 검증
        Job job = jobRepository.findByIdAndUserIdAndDeletedFalse(jobId, farmerUserId)
                .orElseThrow(() -> new NotFoundException("공고 정보 없음"));

        // 지원한 worker 목록(지원서+지원자 조인)
        List<Application> applications = applicationRepository.findByJobIdAndDeletedFalse(jobId);
//        List<WorkerDto> workerDtos = new ArrayList<>();
//        for (Application app : applications) {
//            WorkerDto dto = new WorkerDto(app.getWorker());
//            // AI 매칭 점수 계산(농가-근로자)
//            Double aiScore = aiClient.recommendWorkersForFarmer(farmerUserId, dto.getUserId());
//            dto.setAiScore(aiScore);
//            workerDtos.add(dto);
//        }
        // Batch로 AI 1회만 호출
        List<Long> workerIds = applications.stream()
                .map(app -> app.getWorker().getUserId())
                .collect(Collectors.toList());
        Map<Long, Double> matchScores = aiClient.getBatchMatchScores(farmerUserId, jobId, workerIds); // 1회 호출

        List<WorkerDto> workerDtos = applications.stream()
                .map(app -> {
                    WorkerDto dto = new WorkerDto(app.getWorker());
                    dto.setAiScore(matchScores.get(dto.getUserId()));
                    return dto;
                })
                .sorted(Comparator.comparing(WorkerDto::getAiScore, Comparator.nullsLast(Comparator.reverseOrder()))) // AI 점수 높은 순으로 정렬(내림차순)
                .collect(Collectors.toList());

        return workerDtos;
    }
}
