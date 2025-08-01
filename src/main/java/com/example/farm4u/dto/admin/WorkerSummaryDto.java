package com.example.farm4u.dto.admin;

import java.time.LocalDate;

public class WorkerSummaryDto {
    private Long userId;
    private String name;                  // 닉네임/성함
    private LocalDate birthDate;
    private String phoneNumber;
    private String activeArea;            // 활동지역(주소 앞부분)
    private int applicationCount;         // 지원내역(숫자)
    private int matchedCount;             // 매칭내역(숫자)

    // getters, setters
}
