//package com.example.farm4u.repository;
//
//import com.example.farm4u.AiClient;
//import com.example.farm4u.dto.job.JobDto;
//import com.example.farm4u.dto.worker.WorkerDto;
//import com.example.farm4u.dto.ai.AutoWriteJobRequest;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class AiRepository {
//    private final AiClient aiClient;
//
//    public AiRepository(AiClient aiClient) {
//        this.aiClient = aiClient;
//    }
//
//    public List<JobDto> findRecommendedJobsForWorker(Long workerUserId) {
//        // DB 직접 조회가 아니라, AI 서버 REST 호출
//        return aiClient.recommendJobsForWorker(workerUserId);
//    }
//
//    public List<WorkerDto> findRecommendedWorkersForFarmer(Long farmerUserId) {
//        return aiClient.recommendWorkersForFarmer(farmerUserId);
//    }
//
//    public JobDto generateAutoJobPosting(Long farmerUserId, AutoWriteJobRequest req) {
//        return aiClient.autoWriteJob(farmerUserId, req);
//    }
//
//    public void recalculateTrustScore(Long targetId, Long reviewId, String targetType) {
//        aiClient.autoRateReview(targetId, reviewId, targetType);
//    }
//}
