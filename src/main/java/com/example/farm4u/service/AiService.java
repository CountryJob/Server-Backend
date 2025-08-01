package com.example.farm4u.service;

import com.example.farm4u.AiClient;
import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.ai.AutoWriteJobRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiService {

    private final AiClient aiClient;

    public AiService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    // 1. 구직자에게 job 추천
    public List<JobDto> recommendJobsForWorker(Long workerUserId) {
        return aiClient.recommendJobsForWorker(workerUserId);
    }

    // 2. 농가에 worker 추천
//    public List<WorkerDto> recommendWorkersForFarmer(Long farmerUserId, Long jobId) {
//        return aiClient.recommendWorkersForFarmer(farmerUserId, jobId);
//    }
    public Double recommendWorkersForFarmer(Long farmerUserId, Long workerUserId) {
        return aiClient.recommendWorkersForFarmer(farmerUserId, workerUserId);
    }

    // 3. 공고 자동 작성
    public JobDto autoWriteJob(Long farmerUserId, AutoWriteJobRequest req) {
        return aiClient.autoWriteJob(farmerUserId, req);
    }

    // 4. 비동기 후기 분석 신뢰점수 (백그라운드/이벤트로 트리거하면 최적)
    public void updateTrustScoreAsync(Long targetId, Long reviewId, String targetType) {
        aiClient.autoRateReview(targetId, reviewId, targetType);
    }
}
