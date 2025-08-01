package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserModeMissingException extends RuntimeException {
    public UserModeMissingException() {
        super("신규 가입 시 mode 입력 필수: mode가 입력되지 않았습니다.");
    }
}