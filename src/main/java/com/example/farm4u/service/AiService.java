package com.example.farm4u.service;

import com.example.farm4u.AiClient;
import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.ai.AutoWriteJobRequest;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.dto.review.ReviewDto;
import com.example.farm4u.entity.Farmer;
import com.example.farm4u.entity.Review;
import com.example.farm4u.entity.Worker;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.FarmerRepository;
import com.example.farm4u.repository.ReviewRepository;
import com.example.farm4u.repository.WorkerRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AiService {

    private final AiClient aiClient;
    private final ReviewRepository reviewRepository;
    private final WorkerRepository workerRepository;
    private final FarmerRepository farmerRepository;

    public AiService(AiClient aiClient, ReviewRepository reviewRepository, WorkerRepository workerRepository, FarmerRepository farmerRepository) {
        this.aiClient = aiClient;
        this.reviewRepository = reviewRepository;
        this.workerRepository = workerRepository;
        this.farmerRepository = farmerRepository;
    }

    // 1, 2 -> Job 에서 처리
    // 1. 구직자에게 jobs 추천
    // 2. 농가에 workers 추천

    // 3. 공고 자동 작성
    // 1) 질문-응답 하나당 ai 한번씩 요청해야함: 1개의 질문/음성 응답에 대해 단건 자동 인식
    // req: question_key(ex. work_type), audio_file
    // res: key, transcribed(String) -> jobId의 key 필드를 transcribed로 저장해야함 (아니면 아예 그냥 프론트로 일단 반환)
    public String autoWriteField(String questionKey, MultipartFile audioFile) {
        try {
            return aiClient.autoWriteField(questionKey, audioFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 2) 모든 질문-응답이 끝나면 autoWriteJob을 호출해 자동 작성을 요청해야함
    // req: null이 아닌(응답으로 받은) jobDto 필드
    // res: jobDto 전체 필드
//    public JobDto autoWriteJob(Long userId, JobRequest jobRequest) {
//        JobDto j = aiClient.autoWriteJob(jobRequest);
//        j.setUserId(userId);
//        return j;
//    }

    // 4. 비동기 후기 분석 신뢰점수 (백그라운드/이벤트로 트리거하면 최적)
    /**
     * - targetId: userId
     * - targetType: "WORKER" 또는 "FARMER"
     * - reviewId 대신 reviewDto 전달로 변경해야 함 ->
     * - content
     * - WORKER: sincerity_rating, promise_rating, skill_rating, rehire_rating
     * - FARMER: communication_rating, environment_rating, clarity_rating, reward_rating
     */
    @Async
    public void updateAiScoreAsync(Long targetId, Long reviewId, String targetType) {
        // 1. reviewId로 리뷰 엔티티 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        ReviewDto reviewDto = new ReviewDto(review);
        Double aiScore = aiClient.autoRateReview(targetId, reviewDto, targetType);

        // 해당 유저의 aiScore 컬럼에 반영
        if ("WORKER".equalsIgnoreCase(targetType)) {
            Worker worker = workerRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("근로자 없음: " + targetId));
            worker.setTrustScore(aiScore);
            workerRepository.save(worker);
        }
        else if ("FARMER".equalsIgnoreCase(targetType)) {
            Farmer farmer = farmerRepository.findByUserId(targetId)
                    .orElseThrow(() -> new NotFoundException("농가 없음: " + targetId));
            farmer.setTrustScore(aiScore);
            farmerRepository.save(farmer);
        }
    }
}
