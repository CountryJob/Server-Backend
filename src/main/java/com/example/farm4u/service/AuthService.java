package com.example.farm4u.service;

import com.example.farm4u.exception.InvalidCodeException;
import com.example.farm4u.config.JwtProvider;
import com.example.farm4u.dto.auth.AuthResponse;
import com.example.farm4u.entity.User;
import com.example.farm4u.exception.UserModeMissingException;
import com.example.farm4u.repository.AuthRepository;
import com.example.farm4u.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final SmsService smsService;
    private final JwtProvider jwtProvider;


    @Autowired
    public AuthService(UserRepository userRepository, AuthRepository authRepository, SmsService smsService, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.smsService = smsService;
        this.jwtProvider = jwtProvider;
    }

    /** 인증번호 발송 */
    public String sendAuthCode(String phone) {
        String code = generateRandomCode();
        authRepository.saveVerificationCode(phone, code, 180); // 3분
        // TODO: 실제로는 sendSms 개발 중에만 sendSmsDev
        smsService.sendSmsDev(phone, code);
//        smsService.sendSms(phone, code);
        return code;
    }

    /** 인증번호 검증 + 성공 시 자동 로그인||회원가입 */
    @Transactional
    public AuthResponse verifyCodeAndLogin(String phoneNumber, String code, String mode){
        
        // 인증번호 검증
        String savedCode = authRepository.getVerificationCode(phoneNumber);
        if (!Objects.equals(code, savedCode))
            throw new InvalidCodeException();

        // 로그인 Or 회원가입
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    if(mode == null) throw new UserModeMissingException();
                    User newUser = User.builder()
                            .phoneNumber(phoneNumber)
                            .currentMode(User.Mode.valueOf(mode.toUpperCase()))
                            .deleted(false) // 가입 시 deleted = false default
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtProvider.createToken(user.getId());

        authRepository.deleteVerificationCode(phoneNumber);
        return new AuthResponse(token, user.getId());
    }

    /** 로그아웃 */
    public void logout(){
        // (필요하면 JWT 블랙리스트 처리 또는 RefreshToken Redis 삭제 등 구현)
    }

    /** 랜덤 6자리 인증번호 생성 */
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
