package com.example.farm4u.controller;

import com.example.farm4u.dto.farmer.FarmerRegisterRequest;
import com.example.farm4u.dto.farmer.FarmerUpdateRequest;
import com.example.farm4u.dto.farmer.FarmerResponse;
import com.example.farm4u.dto.user.UserResponse;
import com.example.farm4u.exception.AccessException;
import com.example.farm4u.service.FarmerService;
import com.example.farm4u.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farmers")
public class FarmerController {

    private final FarmerService farmerService;
    private final UserService userService;

    public FarmerController(FarmerService farmerService, UserService userService) {
        this.farmerService = farmerService;
        this.userService = userService;
    }

    /** (내) 농가 등록: name, businessNumber, address만 필수 */
    @PostMapping
    public ResponseEntity<FarmerResponse> registerFarmer(
            @AuthenticationPrincipal Long userId,
            @RequestBody FarmerRegisterRequest request) {
        FarmerResponse resp = farmerService.registerFarmer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /** (내) 농가 정보 수정: 원하는 필드만 전달(Partial), 주소 변경 시 위경도 자동변경 */
    @PatchMapping
    public ResponseEntity<FarmerResponse> updateFarmer(
            @AuthenticationPrincipal Long userId,
            @RequestBody FarmerUpdateRequest request) {
        FarmerResponse resp = farmerService.updateFarmer(userId, request);
        return ResponseEntity.ok(resp);
    }

    /** (내) 농가 정보 조회 */
    @GetMapping
    public ResponseEntity<FarmerResponse> getMyFarmer(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(farmerService.getMyFarmer(userId));
    }

    /** 특정 농가 정보 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<FarmerResponse> getFarmerById(@PathVariable Long id) {
        return ResponseEntity.ok(farmerService.getFarmerById(id));
    }

    /** 농가 리스트 전체 조회 */
    @GetMapping("/list")
    public ResponseEntity<List<FarmerResponse>> listFarmers(@AuthenticationPrincipal Long userId) {
        // 실제 - 주석 제거
//        UserResponse user = userService.getUserById(userId);
//        if (user.getCurrentMode() != "ADMIN"){
//            throw new AccessException("관리자");
//        }
        return ResponseEntity.ok(farmerService.listFarmers());
    }

    /** 근처 농가 조회 */
    @GetMapping(params = {"lat", "lng", "radius"})
    public ResponseEntity<List<FarmerResponse>> listNearbyFarmers(
            @AuthenticationPrincipal Long userId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius) {

        UserResponse user = userService.getUserById(userId);
        if (user.getCurrentMode() != "WORKER"){
            throw new AccessException("작업자");
        }

        return ResponseEntity.ok(farmerService.listNearbyFarmers(lat, lng, radius));
    }



}
