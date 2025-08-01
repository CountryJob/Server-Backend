package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateFarmerException extends RuntimeException {
    public DuplicateFarmerException() { super("이미 등록된 농가입니다."); }
}
