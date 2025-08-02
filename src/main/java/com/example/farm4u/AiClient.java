package com.example.farm4u;

import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.dto.review.ReviewDto;
import com.example.farm4u.dto.worker.WorkerDto;
import com.example.farm4u.entity.Worker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class AiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.base-url}")
    private String aiServerBaseUrl; // 포트 번호 분리해 설정하기 ex) 8000

    // 1. 추천받기: 구직자용
    /**
     * req:
     *  - worker의 gender, has_farm_exp, ai_score, work_days
     *  - 전체 jobs (JobDto 등)
     * res: Map<Long, Double> (jobId별 추천 점수)
     */
    public Map<Long, Double> recommendJobsForWorker(Worker.Gender gender, Boolean hasFarmExp, Double aiScore, Set<Worker.WorkDay> workDays, List<JobDto> allJobs) {
        String url = aiServerBaseUrl + "/recommend/jobs-to-worker";

        Map<String, Object> body = new HashMap<>();
        body.put("gender", gender);
        body.put("has_farm_exp", hasFarmExp);
        body.put("ai_score", aiScore);
        body.put("work_days", workDays);
        body.put("jobs", allJobs);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 응답: { jobId: score, ... }
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        Map result = response.getBody();
        Map<Long, Double> scores = new HashMap<>();
        if (result != null) {
            for (Object key : result.keySet()) {
                Long jobId = Long.valueOf(key.toString());
                Double score = result.get(key) != null ? Double.valueOf(result.get(key).toString()) : 0.0;
                scores.put(jobId, score);
            }
        }
        return scores;
    }

    // 2. 추천받기: 농가용
    // 특정 공고에 대한 전체 구직자의 matching score (Batch로 처리)

    /**
     * req:
     *  - job의 start_date, end_date, experience_required, recruit_count_male, recruit_count_female
     *  - 전체 workers (WorkerDto 등)
     * res: Map<Long, Double>  (workerId별 매칭점수)
     */
    public Map<Long, Double> getBatchMatchScores(
            String startDate,
            String endDate,
            Boolean experienceRequired,
            Integer recruitCountMale,
            Integer recruitCountFemale,
            List<WorkerDto> allWorkers
    ) {
        String url = aiServerBaseUrl + "/recommend/workers-to-job";

        Map<String, Object> body = new HashMap<>();
        body.put("start_date", startDate);
        body.put("end_date", endDate);
        body.put("experience_required", experienceRequired);
        body.put("recruit_count_male", recruitCountMale);
        body.put("recruit_count_female", recruitCountFemale);
        body.put("workers", allWorkers);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 응답: { workerId: score, ... }
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        Map result = response.getBody();
        Map<Long, Double> scores = new HashMap<>();
        if (result != null) {
            for (Object key : result.keySet()) {
                Long workerId = Long.valueOf(key.toString());
                Double score = result.get(key) != null ? Double.valueOf(result.get(key).toString()) : 0.0;
                scores.put(workerId, score);
            }
        }
        return scores;
    }

    /**
     // 특정 공고에 대한 특정 구직자의 matching score
     public Double recommendWorkersForFarmer(Long farmerUserId, Long workerUserId) {
     String url = aiServerBaseUrl + "/recommend/workers";
     Map<String, Object> body = Map.of("userId", farmerUserId);
     ResponseEntity<Double> response = restTemplate.postForEntity(url, body, Double.class);
     return response.getBody() != null ? response.getBody() : 0;
     }*/

    // 3. 공고 자동작성 요청
    // AI 서버에게 Multipart 음성 파일 한 번에 전달 -> JobDto 전체 반환
    public JobDto autoWriteJob(MultipartFile audioFile) throws IOException {
        String url = aiServerBaseUrl + "/auto-filled/predict";

//        File tempFile = File.createTempFile("audioFileTemp", ".webm");
        File tempFile = File.createTempFile("audioFileTemp", ".mp4");
        audioFile.transferTo(tempFile);
        FileSystemResource fileResource = new FileSystemResource(tempFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audioFile", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<JobDto> response = restTemplate.postForEntity(url, requestEntity, JobDto.class);

        tempFile.delete(); // 임시 파일 정리
        return response.getBody();
    }

    // 1) 필드별 음성 인식
    // req: question_key(ex. work_type), audio_file
    // res: key, transcribed(String) -> jobId의 key 필드를 transcribed로 저장해야함 (아니면 아예 그냥 프론트로 일단 반환)
    public String autoWriteField(String questionKey, MultipartFile audioFile) throws IOException{
        String url = aiServerBaseUrl + "/voice/ask-and-transcribe";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("question_key", questionKey);

        // MultipartFile을 임시 파일로 변환
        File tempFile = File.createTempFile("voice", ".webm");
        audioFile.transferTo(tempFile);
        FileSystemResource fileResource = new FileSystemResource(tempFile);
        body.add("audio", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
        tempFile.delete();

        // res: { "key": ..., "transcribed": ... }
        Map<String, Object> res = response.getBody();
        if (res == null) throw new RuntimeException("AI 필드 인식 응답 오류");
        return (String) res.get("transcribed");
    }

    // 2)
    // req: null이 아닌(응답으로 받은) jobDto 필드
    //- title
    //- area_size
    //- start_date
    //- end_date
    //- work_time -> start_time, end_time
    // res: jobDto 전체 필드
    //- description
    //- salary_male
    //- salary_female
    //- recruit_count_male
    //- recruit_count_female
    public JobDto autoWrite(JobRequest jobRequest) {
        String url = aiServerBaseUrl + "/auto-filled/predict";

        // 요청 데이터 준비
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("title", jobRequest.getTitle());
        body.add("area_size", jobRequest.getAreaSize());
        body.add("start_date", jobRequest.getStartDate());
        body.add("end_date", jobRequest.getEndDate());
        body.add("start_time", jobRequest.getStartTime());
        body.add("end_time", jobRequest.getEndTime());

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<JobDto> response = restTemplate.postForEntity(
                url,
                new org.springframework.http.HttpEntity<>(body, headers),
                JobDto.class
        );
        return response.getBody();
    }

    // 4. AI 점수 요청 (비동기 트리거)
    public Double autoRateReview(Long targetId, ReviewDto reviewDto, String targetType) {
        String url = aiServerBaseUrl + "/auto-rate/review";

        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("userId", targetId);
        aiRequest.put("targetType", targetType);
        aiRequest.put("content", reviewDto.getContent());

        if ("WORKER".equalsIgnoreCase(targetType)) {
            aiRequest.put("sincerity_rating", reviewDto.getSincerityRating());
            aiRequest.put("promise_rating", reviewDto.getPromiseRating());
            aiRequest.put("skill_rating", reviewDto.getSkillRating());
            aiRequest.put("rehire_rating", reviewDto.getRehireRating());
        } else if ("FARMER".equalsIgnoreCase(targetType)) {
            aiRequest.put("communication_rating", reviewDto.getCommunicationRating());
            aiRequest.put("environment_rating", reviewDto.getEnvironmentRating());
            aiRequest.put("clarity_rating", reviewDto.getClarityRating());
            aiRequest.put("reward_rating", reviewDto.getRewardRating());
        } else {
            throw new IllegalArgumentException("지원하지 않는 targetType: " + targetType);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Double> response = restTemplate.postForEntity(
                url,
                new org.springframework.http.HttpEntity<>(aiRequest, headers),
                Double.class
        );

        return response.getBody() != null ? response.getBody() : 0.0;
    }

}
