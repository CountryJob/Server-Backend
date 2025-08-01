package com.example.farm4u.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoWriteJobRequest {
    // 농장(농가) 기본 정보 (farmer 테이블에서 조회)
    private String address;                // 농가 주소
    private Integer areaSize;              // 농지 면적 (m^2)

    private String title;                   // 작업명

    // 작업 내용(자유입력 텍스트) // nullable ? (기획에 따라)
    private String jobDescription;         // 사용자가 입력한 작업내용
}