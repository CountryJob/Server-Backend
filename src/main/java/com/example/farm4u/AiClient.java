package com.example.farm4u;

import com.example.farm4u.dto.ai.AutoJobResponse;
import com.example.farm4u.dto.job.JobDto;
import com.example.farm4u.dto.job.JobRequest;
import com.example.farm4u.dto.review.ReviewDto;
import com.example.farm4u.dto.worker.WorkerDto;
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
    public Map<Long, Double> recommendJobsForWorker(String gender, Boolean hasFarmExp, Double trustScore, Set<String> workDays, List<JobDto> allJobs) {
        String url = aiServerBaseUrl + "/recommend/jobs-to-worker";

        Map<String, Object> workerMap = new HashMap<>();
        workerMap.put("gender", gender);
        workerMap.put("has_farm_exp", hasFarmExp);
        workerMap.put("trust_score", trustScore);
        workerMap.put("work_days", workDays);
        workerMap.put("jobs", allJobs);

        List<Map<String, Object>> jobsList = new ArrayList<>();
        for (JobDto job : allJobs) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", job.getId());
            jobMap.put("start_date", job.getStartDate().toString());   // String or ISO
            jobMap.put("end_date", job.getEndDate().toString());
            jobMap.put("experience_required", job.getExperienceRequired());
            jobMap.put("recruit_count_male", job.getRecruitCountMale());
            jobMap.put("recruit_count_female", job.getRecruitCountFemale());
            jobsList.add(jobMap);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("worker", workerMap);
        body.put("jobs", jobsList);

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

        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("start_date", startDate);
        jobMap.put("end_date", endDate);
        jobMap.put("experience_required", experienceRequired);
        jobMap.put("recruit_count_male", recruitCountMale);
        jobMap.put("recruit_count_female", recruitCountFemale);

        Map<String, Object> body = new HashMap<>();
        body.put("job", jobMap);
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
    /**
     * req:
     *
     * @param audioFile res:
     *                  {
     *                  "title": "상추 수확",
     *                  "startDate": "2025-08-01",
     *                  "endDate": "2025-08-03",
     *                  "startTime": "08:00:00",
     *                  "endTime": "17:00:00",
     *                  "areaSize": 50,
     *                  "meal": true,
     *                  "snack": true,
     *                  "transportAllowance": false,
     *                  "addressMatch": true,
     *                  "description": "- 상추 수확 및 선별\n- 수확물 박스 포장 및 운반 보조\n- 작업 도구 세척 및 정리\n- 작업장 주변 환경 정돈",
     *                  "salaryMale": 120000,
     *                  "salaryFemale": 120000,
     *                  "recruitCountMale": 1,
     *                  "recruitCountFemale": 1
     *                  }
     */
    public AutoJobResponse autoWriteJob(MultipartFile audioFile) throws IOException {
        String url = aiServerBaseUrl + "/auto-filled/predict";

        File tempFile = File.createTempFile("audioFileTemp", ".m4a");
        audioFile.transferTo(tempFile);
        FileSystemResource fileResource = new FileSystemResource(tempFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio_file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<AutoJobResponse> response = restTemplate.postForEntity(url, requestEntity, AutoJobResponse.class);

        tempFile.delete(); // 임시 파일 정리
        return response.getBody();
    }

    // 사용X
    // 1) 필드별 음성 인식
    // req: question_key(ex. work_type), audio_file
    // res: key, transcribed(String) -> jobId의 key 필드를 transcribed로 저장해야함 (아니면 아예 그냥 프론트로 일단 반환)
    public String autoWriteField(String questionKey, MultipartFile audioFile) throws IOException{
        String url = aiServerBaseUrl + "/voice/ask-and-transcribe";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("question_key", questionKey);

        // MultipartFile을 임시 파일로 변환
        File tempFile = File.createTempFile("voice", ".m4a");
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

    // 사용X
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
