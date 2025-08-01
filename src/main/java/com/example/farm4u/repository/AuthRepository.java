package com.example.farm4u.repository;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class AuthRepository {
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

//    public void saveVerificationCode(String phoneNumber, String code, long ttlSeconds) {
//        redisTemplate.opsForValue().set(phoneNumber, code, ttlSeconds, TimeUnit.SECONDS);
//    }
//
//    public String getVerificationCode(String phoneNumber) {
//        return redisTemplate.opsForValue().get(phoneNumber);
//    }
//
//    public void deleteVerificationCode(String phoneNumber) {
//        // 보안용
//        redisTemplate.delete(phoneNumber);
//    }

    @AllArgsConstructor
    private static class CodeWithExpire {
        String code;
        long expireAt;
    }

    private final Map<String, CodeWithExpire> codeStore = new ConcurrentHashMap<>();

    public void saveVerificationCode(String phoneNumber, String code, long ttlSeconds) {
        codeStore.put(phoneNumber, new CodeWithExpire(code, System.currentTimeMillis() + ttlSeconds * 1000));
    }

    public String getVerificationCode(String phoneNumber) {
        CodeWithExpire info = codeStore.get(phoneNumber);
        if (info == null || info.expireAt < System.currentTimeMillis()) {
            codeStore.remove(phoneNumber);
            return null;
        }
        return info.code;
    }
}