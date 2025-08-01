package com.example.farm4u.controller;

import com.example.farm4u.dto.auth.AuthRequest;
import com.example.farm4u.dto.auth.AuthVerifyRequest;
import com.example.farm4u.dto.auth.AuthResponse;
import com.example.farm4u.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /** 인증번호 요청 */
//    @PostMapping("/request-code")
    public ResponseEntity<Void> requestAuthCode(@RequestBody AuthRequest request){
        authService.sendAuthCode(request.getPhoneNumber());
        return ResponseEntity.ok().build();
    }

    // 실제 운영시엔 Void로 돌리고, 개발중에만 코드 응답
    @PostMapping("/request-code")
    public ResponseEntity<String> requestAuthCodeInDevelop(@RequestBody AuthRequest request){
        String code = authService.sendAuthCode(request.getPhoneNumber());
        return ResponseEntity.ok(code);
    }

    /** 인증번호 검증 
     * 신규면 가입 + mode 동시 처리
     * */
    @PostMapping("/verify-code")
    public ResponseEntity<AuthResponse> verifyAuthCode(@RequestBody AuthVerifyRequest request){
        return ResponseEntity.ok(authService.verifyCode(request.getPhoneNumber(), request.getCode(), request.getMode()));
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        authService.logout();
        return ResponseEntity.ok().build();
    }

}
