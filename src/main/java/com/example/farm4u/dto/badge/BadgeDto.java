package com.example.farm4u.dto.badge;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class BadgeDto {
    private Long id;            // 뱃지 id
    private String title;       // 뱃지명
    private String description; // 설명
    private String imgUrl;      // 이미지 경로
    private String badgeType;   // ENUM: PUNCTUALITY 등
    private String badgeCondition; // 지급조건 설명(문자열)
}
