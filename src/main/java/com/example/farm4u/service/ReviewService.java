package com.example.farm4u.service;

import com.example.farm4u.dto.review.ReviewDto;
import com.example.farm4u.dto.review.ReviewRequest;
import com.example.farm4u.entity.Farmer;
import com.example.farm4u.entity.Review;
import com.example.farm4u.entity.Worker;
import com.example.farm4u.exception.NotFoundException;
import com.example.farm4u.repository.FarmerRepository;
import com.example.farm4u.repository.ReviewRepository;
import com.example.farm4u.repository.UserRepository;
import com.example.farm4u.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReviewService
 * - userId로 users 테이블 currentMode(권한) 실시간 조회 분기
 * - 리뷰 등록/수정/삭제 시 workers 테이블의 집계 컬럼 실시간 동기화*/

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final FarmerRepository farmerRepository;
    private final AiService aiService;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         WorkerRepository workerRepository,
                         FarmerRepository farmerRepository, AiService aiService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.farmerRepository = farmerRepository;
        this.aiService = aiService;
    }

    /**
     * 후기 등록: userId는 principal에서, userMode(권한)는 users 테이블에서 실시간 조회하여 분기!
     * 등록/수정/삭제 마다 worker 집계(평균점수, reviewCount 등) 즉시 동기화.
     */
    @Transactional
    public ReviewDto createReview(Long userId, ReviewRequest req) {
        String userMode = userRepository.findCurrentModeById(userId);  // users 테이블에서 권한/모드 조회

        Review review;
        if ("WORKER".equalsIgnoreCase(userMode)) {
            if (reviewRepository.findByUserIdAndJobIdAndWorkerUserIdIsNullAndDeletedFalse(userId, req.getJobId()).isPresent()) {
                throw new IllegalStateException("이미 이 공고에 대한 후기를 작성했습니다.");
            }
            review = Review.builder()
                    .userId(userId)
                    .jobId(req.getJobId())
                    .workerUserId(null)
                    .communicationRating(req.getCommunicationRating())
                    .environmentRating(req.getEnvironmentRating())
                    .clarityRating(req.getClarityRating())
                    .rewardRating(req.getRewardRating())
                    .content(req.getContent())
                    .deleted(false)
                    .build();
        } else if ("FARMER".equalsIgnoreCase(userMode)) {
            if (reviewRepository.findByUserIdAndWorkerUserIdAndJobIdAndDeletedFalse(userId, req.getWorkerUserId(), req.getJobId()).isPresent()) {
                throw new IllegalStateException("이미 이 근로자의 해당 작업에 대한 후기를 작성했습니다.");
            }
            review = Review.builder()
                    .userId(userId)
                    .jobId(req.getJobId())
                    .workerUserId(req.getWorkerUserId())
                    .sincerityRating(req.getSincerityRating())
                    .promiseRating(req.getPromiseRating())
                    .skillRating(req.getSkillRating())
                    .rehireRating(req.getRehireRating())
                    .content(req.getContent())
                    .deleted(false)
                    .build();
        } else {
            throw new IllegalArgumentException("사용자 권한정보가 올바르지 않습니다.");
        }
        Review saved = reviewRepository.save(review);

        // 집계반영
        if ("WORKER".equalsIgnoreCase(userMode)) {
            // worker가 job에 남긴 리뷰는 farmReview 집계(공고 소유주 farmer에 대해)
            Long farmerUserId = userRepository.findFarmerUserIdByJobId(req.getJobId());
            if (farmerUserId != null) {
//                updateFarmerReviewAggregates(farmerUserId);
                aiService.updateAiScoreAsync(farmerUserId, saved.getId(), "FARMER");
            }
        } else {
            // 농가가 worker에 남긴 리뷰면 worker 집계
//            updateWorkerReviewAggregates(req.getWorkerUserId());
            aiService.updateAiScoreAsync(review.getWorkerUserId(), review.getId(), "WORKER");
        }

        return new ReviewDto(saved);
    }

    /** 수정 (집계도 동기) */
    @Transactional
    public ReviewDto updateReview(Long userId, Long reviewId, ReviewRequest req) {
        String userMode = userRepository.findCurrentModeById(userId);

        Review review = reviewRepository.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new NotFoundException("후기"));

        if (req.getContent() != null) review.setContent(req.getContent());
        if (req.getSincerityRating() != null) review.setSincerityRating(req.getSincerityRating());
        if (req.getPromiseRating() != null) review.setPromiseRating(req.getPromiseRating());
        if (req.getSkillRating() != null) review.setSkillRating(req.getSkillRating());
        if (req.getRehireRating() != null) review.setRehireRating(req.getRehireRating());
        if (req.getCommunicationRating() != null) review.setCommunicationRating(req.getCommunicationRating());
        if (req.getEnvironmentRating() != null) review.setEnvironmentRating(req.getEnvironmentRating());
        if (req.getClarityRating() != null) review.setClarityRating(req.getClarityRating());
        if (req.getRewardRating() != null) review.setRewardRating(req.getRewardRating());

        // 집계반영 (분기 동일)
        if (review.getWorkerUserId() == null) {
            // worker→job 리뷰: 집계는 job 소유주(farmer)에!
            Long farmerUserId = userRepository.findFarmerUserIdByJobId(review.getJobId());
            if (farmerUserId != null) {
//                updateFarmerReviewAggregates(farmerUserId);
                aiService.updateAiScoreAsync(farmerUserId, review.getId(), "FARMER");
            }
        } else {
            // 농가→worker 리뷰: worker 집계
//            updateWorkerReviewAggregates(review.getWorkerUserId());
            aiService.updateAiScoreAsync(review.getWorkerUserId(), review.getId(), "WORKER");
        }

        return new ReviewDto(review);
    }

    /** 삭제 (본인만, 삭제시 집계도 동기) */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        String userMode = userRepository.findCurrentModeById(userId);

        Review review = reviewRepository.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new NotFoundException("후기"));
        review.setDeleted(true);

        // 집계반영
        if (review.getWorkerUserId() == null) {
            Long farmerUserId = userRepository.findFarmerUserIdByJobId(review.getJobId());
            if (farmerUserId != null) {
//                updateFarmerReviewAggregates(farmerUserId);
                aiService.updateAiScoreAsync(farmerUserId, review.getId(), "FARMER");
            }
        } else {
//            updateWorkerReviewAggregates(review.getWorkerUserId());
            aiService.updateAiScoreAsync(review.getWorkerUserId(), review.getId(), "WORKER");
        }
    }

    /** 상세 */
    public ReviewDto getReviewDetail(Long reviewId) {
        Review r = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException("후기"));
        return new ReviewDto(r);
    }

    /** worker->(jobs)farmer 후기 리스트 */
    public List<ReviewDto> getWorkerToFarmerReviews(Long farmerUserId) {
        return reviewRepository.findAllByFarmerUserIdFromWorkersAndDeletedFalse(farmerUserId)
                .stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    /** farmer->worker 후기 리스트 조회 */
    public List<ReviewDto> getFarmerToWorkerReviews(Long workerUserId) {
        return reviewRepository.findAllByWorkerUserIdAndDeletedFalse(workerUserId)
                .stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    ///////////////////////////////////////
    // ----- 집계(평균/개수) 분기 구현 -----
    ///////////////////////////////////////

    /**
     * reviews 테이블을 기준으로 
     * 해당 worker의 모든 농가평가(soft deleteX) 평균/개수 구해 workers 테이블에 동기화
     * 해당 farmer의 모든 job평가(soft deleteX) 평균/개수 구해 farmers 테이블에 동기화
     */
    // [1] (worker -> job) 리뷰: "job.owner(farmer)에 대해" 집계
    @Transactional
    public void updateFarmerReviewAggregates(Long farmerUserId) {
        // farmer의 모든 jobId
        List<Review> reviews = reviewRepository.findAllByFarmerIdForFarmReview(farmerUserId);
        int count = reviews.size();

        double comm = avg(reviews, Review::getCommunicationRating);
        double env  = avg(reviews, Review::getEnvironmentRating);
        double clarity = avg(reviews, Review::getClarityRating);
        double reward  = avg(reviews, Review::getRewardRating);

        Optional<Farmer> farmerOpt = farmerRepository.findByUserId(farmerUserId);
        if (farmerOpt.isPresent()) {
            Farmer farmer = farmerOpt.get();
            farmer.setAvgCommunicationRating(count > 0 ? BigDecimal.valueOf(comm) : null);
            farmer.setAvgEnvironmentRating(count > 0 ? BigDecimal.valueOf(env) : null);
            farmer.setAvgClarityRating(count > 0 ? BigDecimal.valueOf(clarity) : null);
            farmer.setAvgRewardRating(count > 0 ? BigDecimal.valueOf(reward) : null);
            farmer.setReviewCount(count);
            farmerRepository.save(farmer);
        }
    }

    // [2] (farmer -> worker) 리뷰는 workerUserId별로 직접 집계
    @Transactional
    public void updateWorkerReviewAggregates(Long workerUserId) {
        List<Review> reviews = reviewRepository.findAllByWorkerUserIdAndDeletedFalse(workerUserId);
        int count = reviews.size();

        double sincerity = avg(reviews, Review::getSincerityRating);
        double promise = avg(reviews, Review::getPromiseRating);
        double skill = avg(reviews, Review::getSkillRating);
        double rehire = avg(reviews, Review::getRehireRating);

        Optional<Worker> workerOpt = workerRepository.findById(workerUserId);
        if (workerOpt.isPresent()) {
            Worker worker = workerOpt.get();
            worker.setAvgSincerityRating(count > 0 ? BigDecimal.valueOf(sincerity) : null);
            worker.setAvgPromiseRating(count > 0 ? BigDecimal.valueOf(promise) : null);
            worker.setAvgSkillRating(count > 0 ? BigDecimal.valueOf(skill) : null);
            worker.setAvgRehireRating(count > 0 ? BigDecimal.valueOf(rehire) : null);
            worker.setReviewCount(count);
            workerRepository.save(worker);
        }
    }

    private double avg(List<Review> reviews, java.util.function.Function<Review, Integer> mapper) {
        List<Integer> v = reviews.stream().map(mapper).filter(x->x!=null).collect(Collectors.toList());
        if (v.isEmpty()) return 0d;
        return v.stream().mapToInt(i->i).average().orElse(0d);
    }
}
