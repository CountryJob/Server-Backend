package com.example.farm4u.dto.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {
    private Long id;
    private String phoneNumber;
    private String currentMode;
    private Boolean deleted;
}
