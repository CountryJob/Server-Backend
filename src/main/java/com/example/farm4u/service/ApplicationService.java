package com.example.farm4u.service;

import com.example.farm4u.dto.application.ApplicationDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.dto.application.ApplicationCreateRequest;
import com.example.farm4u.entity.Application;
import com.example.farm4u.entity.Application.ApplyStatus;
import com.example.farm4u.entity.Job;
import com.example.farm4u.entity.Worker;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.ApplicationRepository;
import com.example.farm4u.repository.JobRepository;
import com.example.farm4u.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              WorkerRepository workerRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.workerRepository = workerRepository;
        this.jobRepository = jobRepository;
    }

    // 1. 지원 상세 조회 (농가/관리자)
    public WorkerDto getApplicationDetail(Long farmerId, Long applicationId) {
        // Repository에서 WorkerDto 프로젝션 반환
        return applicationRepository.findWorkerDtoByApplicationIdAndFarmerId(farmerId, applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역"));
    }

    // 2. 지원 상태 변경
    @Transactional
    protected void updateStatus(Long workerId, Long applicationId, ApplyStatus status) {
        Application application = applicationRepository.findByIdAndWorkerId(applicationId, workerId)
                .orElseThrow(() -> new NotFoundException("지원 내역"));
        application.setApplyStatus(status);
        applicationRepository.save(application);
    }

    // 3. 지원 수락/거절/취소
    public void acceptApplication(Long workerId, Long applicationId) {
        updateStatus(workerId, applicationId, ApplyStatus.MATCHED);
    }

    public void rejectApplication(Long workerId, Long applicationId) {
        updateStatus(workerId, applicationId, ApplyStatus.REJECTED);
    }

    public void revokeApplication(Long workerId, Long applicationId) {
        updateStatus(workerId, applicationId, ApplyStatus.APPLIED);
    }

    // 4. 지원하기
    public void apply(Long workerId, ApplicationCreateRequest req) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new NotFoundException("작업자"));
        Job job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new NotFoundException("공고"));

        Application application = Application.builder()
                .worker(worker)
                .job(job)
                .applyStatus(ApplyStatus.APPLIED)
                .priority(req.getPriority())
                .build();

        applicationRepository.save(application);
    }

    // 5. 내 지원내역(+필터) status가 APPLIED이면 전체, 아니면 해당 status
    public List<ApplicationDto> getMyApplications(Long workerId, ApplyStatus status) {
        if (status == ApplyStatus.APPLIED) {
            // 전체 조회
            return applicationRepository.findByWorkerIdAndDeletedFalse(workerId);
        } else {
            return applicationRepository.findByWorkerIdAndApplyStatusAndDeletedFalse(workerId, status);
        }
    }

    // 6. 내 지원 취소
    public void cancelApplication(Long workerId, Long applicationId) {
        updateStatus(workerId, applicationId, ApplyStatus.CANCELED);
    }
}
