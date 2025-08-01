package com.example.farm4u;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.ai.AutoWriteJobRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class AiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.base-url}")
    private String aiServerBaseUrl; // 포트 번호 분리해 설정하기 ex) 8000

    // 1. 추천받기: 구직자용
    public List<JobDto> recommendJobsForWorker(Long workerUserId) {
        String url = aiServerBaseUrl + "/recommend/jobs";
        Map<String, Object> body = Map.of("userId", workerUserId);
        ResponseEntity<JobDto[]> response = restTemplate.postForEntity(url, body, JobDto[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
    }

    // 2. 추천받기: 농가용
    // 특정 공고에 대한 추천 구직자 리스트.. hmm..
//    public List<WorkerDto> recommendWorkersForFarmer(Long farmerUserId) {
//        String url = aiServerBaseUrl + "/recommend/workers";
//        Map<String, Object> body = Map.of("userId", farmerUserId);
//        ResponseEntity<WorkerDto[]> response = restTemplate.postForEntity(url, body, WorkerDto[].class);
//        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
//    }

    // 특정 공고에 대한 특정 구직자의 matching score
    public Double recommendWorkersForFarmer(Long farmerUserId, Long workerUserId) {
        String url = aiServerBaseUrl + "/recommend/workers";
        Map<String, Object> body = Map.of("userId", farmerUserId);
        ResponseEntity<Double> response = restTemplate.postForEntity(url, body, Double.class);
        return response.getBody() != null ? response.getBody() : 0;
    }

    // 특정 공고에 대한 전체 구직자의 matching score (Batch로 처리)
    public Map<Long, Double> getBatchMatchScores(Long farmerId, Long jobId, List<Long> workerIds) {
        String url = aiServerBaseUrl + "/recommend/workers/batch";

        // 요청 body 생성
        Map<String, Object> body = new HashMap<>();
        body.put("farmerId", farmerId);
        body.put("jobId", jobId);
        body.put("workerIds", workerIds);

        // 헤더: JSON 명시
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity로 합성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 응답은 {workerId: matchScore, ...} Map 형태로 내려온다고 가정
        // AI 서버 응답 예시 = {"101": 0.93, "102": 0.72, ...} 식의 workerId별 점수 map 이어야함
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        // Map<Long, Double> 형태로 변환
        Map result = response.getBody();
        Map<Long, Double> matchScores = new HashMap<>();
        if (result != null) {
            for (Object key : result.keySet()) {
                Long workerId = Long.valueOf(key.toString());
                Double score = result.get(key) != null ? Double.valueOf(result.get(key).toString()) : 0.0;
                matchScores.put(workerId, score);
            }
        }
        return matchScores;
    }


    // 3. 공고 자동작성 요청
    public JobDto autoWriteJob(Long farmerUserId, AutoWriteJobRequest req) {
        String url = aiServerBaseUrl + "/auto-write/job";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", farmerUserId);
        body.put("input", req); // req 객체가 직렬화 가능해야 함
        ResponseEntity<JobDto> response = restTemplate.postForEntity(url, body, JobDto.class);
        return response.getBody();
    }

    // 4. 후기 신뢰점수 요청 (비동기 트리거)
    public void autoRateReview(Long targetId, Long reviewId, String targetType) {
        String url;
        Map<String, Object> body = new HashMap<>();
        body.put("targetId", targetId);
        body.put("reviewId", reviewId);

        // 타겟 타입에 따라 각각 다른 AI 서버 엔드포인트나 파라미터로 분기
        if ("WORKER".equalsIgnoreCase(targetType)) {
            url = aiServerBaseUrl + "/auto-rate/review/worker";
            // 필요시 추가 데이터
        } else if ("FARMER".equalsIgnoreCase(targetType)) {
            url = aiServerBaseUrl + "/auto-rate/review/farmer";
            // 필요시 추가 데이터
        } else {
            throw new IllegalArgumentException("지원하지 않는 targetType: " + targetType);
        }

        restTemplate.postForEntity(url, body, Void.class);
    }

}
