package com.example.farm4u.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException() { super("이미 등록된 전화번호입니다."); }
}
