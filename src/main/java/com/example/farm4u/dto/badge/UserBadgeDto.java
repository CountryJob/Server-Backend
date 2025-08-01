package com.example.farm4u.dto.badge;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserBadgeDto {
    private Long id;
    private Long badgeId;
    private String title;
    private String description;
    private String imgUrl;
    private String badgeType;
    private String badgeCondition;
    private String createdAt;
}
