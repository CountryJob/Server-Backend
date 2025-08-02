package com.example.farm4u.service;

import com.example.farm4u.dto.report.ReportDto;
import com.example.farm4u.dto.report.ReportCreateRequest;
import com.example.farm4u.entity.Report;
import com.example.farm4u.repository.FarmerRepository;
import com.example.farm4u.repository.ReportRepository;
import com.example.farm4u.repository.UserRepository;
import com.example.farm4u.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final FarmerRepository farmerRepository;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         WorkerRepository workerRepository,
                         FarmerRepository farmerRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.farmerRepository = farmerRepository;
    }

    @Transactional
    public ReportDto createReport(Long userId, ReportCreateRequest req) {
        String userMode = userRepository.findCurrentModeById(userId); // users 테이블에서 mode 조회

        Report report;
        if ("WORKER".equalsIgnoreCase(userMode)) {
            if (reportRepository.findByUserIdAndJobIdAndWorkerUserIdIsNull(userId, req.getJobId()).isPresent()) {
                throw new IllegalStateException("이미 해당 공고에 신고를 남겼습니다.");
            }
            report = Report.builder()
                    .userId(userId)
                    .jobId(req.getJobId())
                    .workerUserId(null)
                    .reason(Report.Reason.valueOf(req.getReason()))
                    .build();

            Report saved = reportRepository.save(report);
            // worker -> job=farmer 신고
            Long farmerUserId = userRepository.findFarmerUserIdByJobId(req.getJobId());
            incrementFarmerReportCount(farmerUserId);
            return new ReportDto(saved);

        } else if ("FARMER".equalsIgnoreCase(userMode)) {
            if (req.getWorkerUserId() == null) {
                throw new IllegalArgumentException("근로자 신고시 workerUserId는 필수입니다.");
            }
            if (reportRepository.findByUserIdAndJobIdAndWorkerUserId(userId, req.getJobId(), req.getWorkerUserId()).isPresent()) {
                throw new IllegalStateException("이미 해당 근로자에 신고를 남겼습니다.");
            }
            report = Report.builder()
                    .userId(userId)
                    .jobId(req.getJobId())
                    .workerUserId(req.getWorkerUserId())
                    .reason(Report.Reason.valueOf(req.getReason()))
                    .build();

            Report saved = reportRepository.save(report);
            // farmer -> worker 신고
            incrementWorkerReportCount(req.getWorkerUserId());
            return new ReportDto(saved);

        } else {
            throw new IllegalArgumentException("잘못된 사용자 권한입니다.");
        }
    }

    /** 전체 신고 리스트(ADMIN 화면) */
    public List<ReportDto> getAllReports() {
        return reportRepository.findAll()
                .stream().map(ReportDto::new).collect(Collectors.toList());
    }

    // 신고 누적 집계 (worker)
    private void incrementWorkerReportCount(Long workerUserId) {
        if (workerUserId == null) return;
        workerRepository.findById(workerUserId).ifPresent(worker -> {
            int cur = (worker.getReportCount() != null) ? worker.getReportCount() : 0;
            worker.setReportCount(cur + 1);
            workerRepository.save(worker);
        });
    }

    // 신고 누적 집계 (worker)
    private void incrementFarmerReportCount(Long farmerUserId) {
        if (farmerUserId == null) return;
        farmerRepository.findByUserId(farmerUserId).ifPresent(farmer -> {
            int cur = (farmer.getReportCount() != null) ? farmer.getReportCount() : 0;
            farmer.setReportCount(cur + 1);
            farmerRepository.save(farmer);
        });
    }

}
