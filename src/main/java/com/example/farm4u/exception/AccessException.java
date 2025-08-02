package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessException extends RuntimeException {
    public AccessException(String accessed) {

        super(accessed + "만 접근 가능합니다.");
    }
}