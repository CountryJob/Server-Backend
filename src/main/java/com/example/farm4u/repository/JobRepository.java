package com.example.farm4u.repository;

import com.example.farm4u.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JobRepository
 * - 거리 필터/농가 JOIN, 모든 주요 조건별 필터, 상세/소유자 조회, 필터 조합 모두 지원!
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    /** 공고 상세(삭제된 것 제외) */
    Optional<Job> findByIdAndDeletedFalse(Long id);

    /** 소유자 + 삭제안된 공고 1건 (수정/삭제 등 권한 체크) */
    Optional<Job> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    /**
     * 거리 + 임금, 기간, 경력, 마감 등 모든 조건 필터 조합 (farmer 테이블 조인!).
     * harversine 공식으로 반경 radius(km) 내 공고만 필터, 결과는 거리순.
     */
    @Query(value =
            "SELECT j.* " +
                    "FROM job_postings j " +
                    "JOIN farmers f ON j.user_id = f.user_id " +
                    "WHERE j.deleted = false AND j.closed = false AND " +
                    "(:experienceRequired IS NULL OR j.experience_required = :experienceRequired) AND " +
                    "(:salaryMaleMin IS NULL OR j.salary_male >= :salaryMaleMin) AND " +
                    "(:salaryFemaleMin IS NULL OR j.salary_female >= :salaryFemaleMin) AND " +
                    "(:startDateFrom IS NULL OR j.start_date >= :startDateFrom) AND " +
                    "(:startDateTo IS NULL OR j.start_date <= :startDateTo) AND " +
                    "(6371 * acos(" +
                    "cos(radians(:lat)) * cos(radians(f.latitude)) * cos(radians(f.longitude) - radians(:lng)) + " +
                    "sin(radians(:lat)) * sin(radians(f.latitude)) )) <= :radius " +
                    "ORDER BY j.created_at DESC",
            nativeQuery = true)
    List<Job> findByAllFiltersAndDistance(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusKm,
            @Param("experienceRequired") Boolean experienceRequired,
            @Param("salaryMaleMin") Integer salaryMaleMin,
            @Param("salaryFemaleMin") Integer salaryFemaleMin,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo);

    /**
     * 거리 조건 없이 임금/경력/마감/기간 기준 필터만 (최신순)
     */
    @Query("SELECT j FROM Job j WHERE j.deleted = false AND j.closed = false " +
            "AND (:experienceRequired IS NULL OR j.experienceRequired = :experienceRequired) " +
            "AND (:salaryMaleMin IS NULL OR j.salaryMale >= :salaryMaleMin) " +
            "AND (:salaryFemaleMin IS NULL OR j.salaryFemale >= :salaryFemaleMin) " +
            "AND (:startDateFrom IS NULL OR j.startDate >= :startDateFrom) " +
            "AND (:startDateTo IS NULL OR j.startDate <= :startDateTo) " +
            "ORDER BY j.createdAt DESC")
    List<Job> findFilteredJobs(
            @Param("experienceRequired") Boolean experienceRequired,
            @Param("salaryMaleMin") Integer salaryMaleMin,
            @Param("salaryFemaleMin") Integer salaryFemaleMin,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo
    );

    /** 특정 농가(작성자) 기준 전체 공고(마이페이지용 등) */
    @Query("SELECT j FROM Job j WHERE j.userId = :userId AND j.deleted = false")
    List<Job> findAllByUserIdAndDeletedFalse(@Param("userId") Long userId);
}
