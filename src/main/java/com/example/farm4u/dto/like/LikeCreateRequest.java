package com.example.farm4u.dto.like;

import lombok.*;

@Getter @Setter
public class LikeCreateRequest {
    private Long jobId;   // 관심 등록할 공고 id
}
