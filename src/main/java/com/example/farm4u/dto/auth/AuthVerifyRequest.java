package com.example.farm4u.dto.auth;

import lombok.Getter;

@Getter
public class AuthVerifyRequest {
    private String phoneNumber;
    private String code;
    private String mode; // 신규가 아니면 null
}
