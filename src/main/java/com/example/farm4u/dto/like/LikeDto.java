package com.example.farm4u.dto.like;

import lombok.*;

@Getter @Setter
public class LikeDto {
    private Long id;         // likes.id
    private Long userId;     // 관심 등록자(구직자) id
    private Long jobId;      // 관심 공고 id
    // deleted
}
