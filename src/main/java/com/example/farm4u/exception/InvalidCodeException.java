package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException() {
        super("인증번호가 일치하지 않거나 만료됐습니다.");
    }
}