package com.example.farm4u.dto.farmer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FarmerResponse {
    private Long userId;
    private String name;
    private String businessNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer areaSize;
    private String description;
}
