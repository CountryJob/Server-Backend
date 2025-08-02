package com.example.farm4u.dto.farmer;

import lombok.Getter;

@Getter
public class FarmerRequest {
    private String name;
    private String businessNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer areaSize;
    private String description;
}
