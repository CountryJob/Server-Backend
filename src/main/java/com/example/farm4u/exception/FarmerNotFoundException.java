package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FarmerNotFoundException extends RuntimeException {
    public FarmerNotFoundException(Long id) {
        super("사용자:" + id + "의 농가 정보가 없습니다.");
    }
}