package com.example.farm4u.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message + " 정보를 찾을 수 없습니다.");
    }
}
