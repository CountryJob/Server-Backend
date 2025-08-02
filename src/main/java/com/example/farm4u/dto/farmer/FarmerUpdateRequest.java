package com.example.farm4u.dto.farmer;

import jakarta.persistence.Column;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FarmerUpdateRequest {
    // 모두 optional, 원하는 것만 포함
    private String name;
    private String businessNumber;
    private String address;
    private Integer areaSize;
    private String description;
    private Double latitude;
    private Double longitude;
}
