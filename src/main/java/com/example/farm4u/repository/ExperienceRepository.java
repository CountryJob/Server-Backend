package com.example.farm4u.repository;

import com.example.farm4u.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // 1. 특정 유저의 작업이력 전체 리스트 (최신순)
    @Query("SELECT e FROM Experience e WHERE e.userId = :userId ORDER BY e.startDate DESC")
    List<Experience> findAllByUserId(@Param("userId") Long userId);

    // 2. 작업이력 상세
    @Query("SELECT e FROM Experience e WHERE e.id = :id AND e.userId = :userId")
    Optional<Experience> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
