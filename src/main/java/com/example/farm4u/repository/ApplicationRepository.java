package com.example.farm4u.repository;

import com.example.farm4u.dto.application.ApplicationDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /** 1. 지원 상세 조회 → WorkerDto 반환 (지원, 농가 기준) */
    @Query("SELECT new com.example.farm4u.dto.worker.WorkerDto(w) " +
            "FROM Application a JOIN a.worker w " +
            "WHERE a.id = :applicationId AND a.job.userId = :farmerId AND a.deleted = FALSE")
    Optional<WorkerDto> findWorkerDtoByApplicationIdAndFarmerId(@Param("farmerId") Long farmerId,
                                                                @Param("applicationId") Long applicationId);

    /** 2. 공고별 지원 Application 엔티티 리스트 (삭제된 건 제외, worker fetch join) */
    @Query("SELECT a FROM Application a " +
            "JOIN FETCH a.worker w " +
            "WHERE a.job.id = :jobId AND a.deleted = FALSE")
    List<Application> findByJobIdAndDeletedFalse(@Param("jobId") Long jobId);

    /** 3. 지원자 userId 리스트 반환 (Delete 제외, 한 공고 기준) */
    @Query("SELECT a.worker.userId FROM Application a " +
            "WHERE a.job.id = :jobId AND a.deleted = FALSE")
    List<Long> findWorkerIdsByJobId(@Param("jobId") Long jobId);

    /** 4. 지원 엔티티 조회 (worker, 지원id, 삭제 안된 것) */
    @Query("SELECT a FROM Application a " +
            "WHERE a.id = :applicationId AND a.worker.userId = :workerId AND a.deleted = FALSE")
    Optional<Application> findByIdAndWorkerId(@Param("applicationId") Long applicationId,
                                              @Param("workerId") Long workerId);

    /** 5. 전체 지원내역 (Delete 제외, DTO 반환) */
    @Query("SELECT new com.example.farm4u.dto.application.ApplicationDto(a) " +
            "FROM Application a " +
            "WHERE a.worker.userId = :workerId AND a.deleted = FALSE")
    List<ApplicationDto> findByWorkerIdAndDeletedFalse(@Param("workerId") Long workerId);

    /** 6. 상태별 지원 (Delete 제외, DTO 반환, applyStatus enum 값) */
    @Query("SELECT new com.example.farm4u.dto.application.ApplicationDto(a) " +
            "FROM Application a " +
            "WHERE a.worker.userId = :workerId AND a.applyStatus = :status AND a.deleted = FALSE")
    List<ApplicationDto> findByWorkerIdAndApplyStatusAndDeletedFalse(@Param("workerId") Long workerId,
                                                                     @Param("status") Application.ApplyStatus status);
}
