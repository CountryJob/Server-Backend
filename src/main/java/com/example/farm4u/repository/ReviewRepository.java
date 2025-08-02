package com.example.farm4u.repository;

import com.example.farm4u.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ReviewRepository
 * - worker/농가 분기, unique 제약 조합별 조회, soft delete, 최신순 리스트 제공
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** worker가 본인->공고로 이미 쓴 리뷰 있는지 (deleted=false) */
    Optional<Review> findByUserIdAndJobIdAndWorkerUserIdIsNullAndDeletedFalse(Long userId, Long jobId);

    /** farmer가 본인+공고->worker 조합으로 이미 쓴 리뷰 있는지 (deleted=false) */
    Optional<Review> findByUserIdAndWorkerUserIdAndJobIdAndDeletedFalse(Long userId, Long workerUserId, Long jobId);

    /** 단일 (삭제X) */
    Optional<Review> findByIdAndDeletedFalse(Long id);

    /** 내가 쓴 후기만 삭제/수정 가능(작성자 확인) */
    Optional<Review> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    // 근로자에 대한 농가평가(집계용)
    @Query("SELECT r FROM Review r WHERE r.workerUserId = :workerUserId AND r.deleted = false")
    List<Review> findAllByWorkerUserIdAndDeletedFalse(Long workerUserId);

    // 농가에 대한 근로자평가(집계용)
    @Query("SELECT r FROM Review r JOIN Job j ON r.jobId = j.id " +
            "WHERE j.userId = :farmerUserId AND r.workerUserId IS NULL AND r.deleted = false")
    List<Review> findAllByFarmerIdForFarmReview(Long farmerUserId);

    @Query("SELECT r FROM Review r JOIN Job j ON r.jobId = j.id " +
            "WHERE j.userId = :farmerUserId AND r.workerUserId IS NULL AND r.deleted = false " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByFarmerUserIdFromWorkersAndDeletedFalse(@Param("farmerUserId") Long farmerUserId);


    /** 특정 job에 대해 workers가 남긴 review 리스트 */
    @Query("SELECT r FROM Review r WHERE r.jobId = :jobId AND r.workerUserId IS NULL AND r.deleted = false ORDER BY r.createdAt DESC")
    List<Review> findByJobIdForWorker(@Param("jobId") Long jobId);

    /** 특정 job에서 farmer가 worker에 남긴 후기 리스트 */
    @Query("SELECT r FROM Review r WHERE r.jobId = :jobId AND r.workerUserId = :workerUserId AND r.deleted = false ORDER BY r.createdAt DESC")
    List<Review> findByJobIdAndWorkerUserIdForFarmer(@Param("jobId") Long jobId, @Param("workerUserId") Long workerUserId);

}
