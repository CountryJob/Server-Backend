package com.example.farm4u.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendSms(String phoneNumber, String code) {
        // 실제 구현은 외부 API(쿨SMS, 네이버 등) 사용
        System.out.println("Send SMS to " + phoneNumber + ": 인증번호는 " + code);
    }
}
