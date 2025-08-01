package com.example.farm4u.controller;

import com.example.farm4u.dto.user.ChangeModeRequest;
import com.example.farm4u.dto.user.UserResponse;
import com.example.farm4u.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 유저 모드 전환 */
    @PatchMapping("/me/mode")
    public ResponseEntity<Void> changeUserMode(@AuthenticationPrincipal Long userId,
                                               @RequestBody ChangeModeRequest request) {
        userService.changeUserMode(userId, request.getMode());
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 유저 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
//            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        // 실제 운영 시 주석 제거
//        if (!user.getCurrentMode().equals(User.Mode.ADMIN)) {
//            throw new AccessDeniedException("관리자만 접근 가능합니다.");
//        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

}
