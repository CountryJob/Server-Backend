package com.example.farm4u.repository;

import com.example.farm4u.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // worker 모드: (userId, jobId) 신고 중복 확인
    Optional<Report> findByUserIdAndJobIdAndWorkerUserIdIsNull(Long userId, Long jobId);

    // farmer 모드: (userId, jobId, workerUserId) 신고 중복 확인
    Optional<Report> findByUserIdAndJobIdAndWorkerUserId(Long userId, Long jobId, Long workerUserId);

    // (admin only) 전체 리스트
    List<Report> findAll();

}
