package com.example.farm4u.service;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.like.LikeDto;
import com.example.farm4u.dto.like.LikeCreateRequest;
import com.example.farm4u.entity.Job;
import com.example.farm4u.entity.Like;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.LikeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LikeService {
    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /** 관심 등록 */
    @Transactional
    public LikeDto createLike(Long userId, LikeCreateRequest req) {
        Optional<Like> exists = likeRepository.findByUserIdAndJobIdAndDeletedFalse(userId, req.getJobId());
        if (exists.isPresent()) {
            Like like = exists.get();
            return toDto(like);
        }
        // UNIQUE user+job, soft delete 처리 주의!
        Like like = Like.builder().userId(userId).jobId(req.getJobId()).deleted(false).build();
        Like saved = likeRepository.save(like);
        return toDto(saved);
    }

    /** 관심 해제 - soft delete /likes/{id} */
    @Transactional
    public void deleteLike(Long userId, Long likeId) {
        Like like = likeRepository.findByIdAndUserIdAndDeletedFalse(likeId, userId)
                .orElseThrow(() -> new NotFoundException("관심(좋아요)"));
        like.setDeleted(true);
        likeRepository.save(like);
    }

    /** 내 좋아요 리스트 */
//    public List<LikeDto> getLikesForUser(Long userId) {
//        return likeRepository.findAllByUserId(userId).stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
    // 좋아요 리스트에 해당하는 관심 Job 리스트
    public List<JobDto> getLikedJobsForUser(Long userId) {
        List<Job> jobs = likeRepository.findLikedJobsByUserId(userId);
        return jobs.stream().map(JobDto::new).collect(Collectors.toList());
    }

    // Entity to DTO 변환 유틸
    private LikeDto toDto(Like like) {
        LikeDto dto = new LikeDto();
        dto.setId(like.getId());
        dto.setUserId(like.getUserId());
        dto.setJobId(like.getJobId());
        return dto;
    }
}
