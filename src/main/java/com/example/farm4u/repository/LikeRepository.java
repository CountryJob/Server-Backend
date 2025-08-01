package com.example.farm4u.repository;

import com.example.farm4u.entity.Job;
import com.example.farm4u.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    // 이미 좋아요했는지(복구 포함)
    Optional<Like> findByUserIdAndJobIdAndDeletedFalse(Long userId, Long jobId);

    // 내 Like 리스트(soft delete 제외)
    @Query("SELECT l FROM Like l WHERE l.userId = :userId AND l.deleted = false")
    List<Like> findAllByUserId(@Param("userId") Long userId);

    /** 내 Like 리스트 -> 관심공고 전체 (soft delete 제외, Job 조인) */
    @Query("SELECT j FROM Like l JOIN Job j ON l.jobId = j.id WHERE l.userId = :userId AND l.deleted = false AND j.deleted = false")
    List<Job> findLikedJobsByUserId(@Param("userId") Long userId);

    // get: soft deleted (userId, likeId -> like) deleted = true 할 like 객체
    Optional<Like> findByIdAndUserIdAndDeletedFalse(Long likeId, Long userId);

    // soft deleted (userId, jobId -> like deleted true)
    @Modifying @Query("UPDATE Like l SET l.deleted = true WHERE l.userId = :userId AND l.jobId = :jobId AND l.deleted = false")
    void softDeleteByUserIdAndJobId(@Param("userId") Long userId, @Param("jobId") Long jobId);

}
