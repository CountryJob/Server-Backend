package com.example.farm4u.repository;

import com.example.farm4u.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    // 삭제되지 않은(논리) '나'의 정보 조회
    @Query("SELECT w FROM Worker w WHERE w.userId = :userId AND w.deleted = false")
    Optional<Worker> findByIdAndDeletedFalse(Long userId);

    // 삭제되지 않은 특정 구직자 조회 (관리자용)
    @Query("SELECT w FROM Worker w WHERE w.userId = :userId AND w.deleted = false")
    Optional<Worker> findByUserIdAndDeletedFalse(Long userId);

    // 삭제되지 않은 전체 구직자 리스트 조회 (관리자용)
    @Query("SELECT w FROM Worker w WHERE w.deleted = false")
    List<Worker> findAllByDeletedFalse();
}
