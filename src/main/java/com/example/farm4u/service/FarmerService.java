package com.example.farm4u.service;

import com.example.farm4u.dto.farmer.FarmerRegisterRequest;
import com.example.farm4u.dto.farmer.FarmerUpdateRequest;
import com.example.farm4u.dto.farmer.FarmerResponse;
import com.example.farm4u.entity.Farmer;
import com.example.farm4u.exception.DuplicateFarmerException;
import com.example.farm4u.exception.FarmerNotFoundException;
import com.example.farm4u.repository.FarmerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final GeocodingService geocodingService;

    public FarmerService(FarmerRepository farmerRepository, GeocodingService geocodingService) {
        this.farmerRepository = farmerRepository;
        this.geocodingService = geocodingService;
    }

    /** (내) 농가 등록: name/businessNumber/address 필수, 주소→위경도 변환 */
    @Transactional
    public FarmerResponse registerFarmer(Long userId, FarmerRegisterRequest request) {
        if (farmerRepository.existsById(userId)) throw new DuplicateFarmerException();

        Double[] latLng = geocodingService.convertAddressToLatLng(request.getAddress());
        Farmer farmer = Farmer.builder()
                .userId(userId)
                .name(request.getName())
                .businessNumber(request.getBusinessNumber())
                .address(request.getAddress())
                .latitude(BigDecimal.valueOf(latLng[0]))
                .longitude(BigDecimal.valueOf(latLng[1]))
                .deleted(false)
                .build();

        Farmer saved = farmerRepository.save(farmer);
        return toResponse(saved);
    }

    /** (내) 농가 정보 update: partial, 주소 바뀌면 위경도 자동 교체 */
    @Transactional
    public FarmerResponse updateFarmer(Long userId, FarmerUpdateRequest request) {
        Farmer farmer = farmerRepository.findById(userId).orElseThrow(() -> new FarmerNotFoundException(userId));

        Farmer.FarmerBuilder builder = farmer.toBuilder();

        if (request.getName() != null)
            builder.name(request.getName());
        if (request.getBusinessNumber() != null)
            builder.businessNumber(request.getBusinessNumber());
        if (request.getAddress() != null) {
            builder.address(request.getAddress());
            Double[] latLng = geocodingService.convertAddressToLatLng(request.getAddress());
            builder.latitude(BigDecimal.valueOf(latLng[0])).longitude(BigDecimal.valueOf(latLng[1]));
        }
        if (request.getAreaSize() != null)
            builder.areaSize(request.getAreaSize());
        if (request.getDescription() != null)
            builder.description(request.getDescription());

        Farmer updated = farmerRepository.save(builder.build());
        return toResponse(updated);
    }

    public FarmerResponse getMyFarmer(Long userId) {
        Farmer farmer = farmerRepository.findById(userId)
                .orElseThrow(() -> new FarmerNotFoundException(userId));
        return toResponse(farmer);
    }

    public FarmerResponse getFarmerById(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new FarmerNotFoundException(id));
        return toResponse(farmer);
    }

    public List<FarmerResponse> listFarmers() {
        return farmerRepository.findByDeletedFalse().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FarmerResponse> listNearbyFarmers(double lat, double lng, double radiusKm) {
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        double latMin = lat - latRange;
        double latMax = lat + latRange;
        double lngMin = lng - lngRange;
        double lngMax = lng + lngRange;
        return farmerRepository.findByLocation(latMin, latMax, lngMin, lngMax).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FarmerResponse toResponse(Farmer farmer) {
        FarmerResponse resp = new FarmerResponse();
        resp.setUserId(farmer.getUserId());
        resp.setName(farmer.getName());
        resp.setBusinessNumber(farmer.getBusinessNumber());
        resp.setAddress(farmer.getAddress());
        resp.setLatitude(farmer.getLatitude() != null ? farmer.getLatitude().doubleValue() : null);
        resp.setLongitude(farmer.getLongitude() != null ? farmer.getLongitude().doubleValue() : null);
        resp.setAreaSize(farmer.getAreaSize());
        resp.setDescription(farmer.getDescription());
        return resp;
    }
}
