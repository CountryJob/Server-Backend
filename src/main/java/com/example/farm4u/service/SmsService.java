package com.example.farm4u.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.from}")
    private String from;

    private final String apiUrl = "https://api.coolsms.co.kr/messages/v4/send";

    public void sendSmsDev(String phoneNumber, String code) {
        System.out.println("phoneNumber: " + phoneNumber + ", code: " + code);
    }
    
    public void sendSms(String phoneNumber, String code) {
        RestTemplate restTemplate = new RestTemplate();

        // CoolSMS 메시지 파라미터
        Map<String, Object> params = new HashMap<>();
        params.put("to", phoneNumber);
        params.put("from", from); // 발신번호 사전 등록 필요
        params.put("text", "[팜포유] 인증번호는 " + code + " 입니다.");
        params.put("type", "SMS");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "HMAC " + apiKey + ":" + apiSecret);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            System.out.println("SMS 발송 결과: " + response.getBody());
        } catch (Exception e) {
            System.err.println("SMS 발송 에러: " + e.getMessage());
        }
    }
}
