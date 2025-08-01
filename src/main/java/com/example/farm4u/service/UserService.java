package com.example.farm4u.service;

import com.example.farm4u.exception.DuplicatePhoneException;
import com.example.farm4u.exception.UserModeMissingException;
import com.example.farm4u.exception.UserNotFoundException;
import com.example.farm4u.dto.user.UserResponse;
import com.example.farm4u.entity.User;
import com.example.farm4u.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(String phoneNumber, String mode) {
        // 1. 유효성 체크
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) throw new DuplicatePhoneException();
        if (mode == null) throw new UserModeMissingException();

        // 2. 엔티티 생성 및 저장
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .currentMode(User.Mode.valueOf(mode.toUpperCase()))
                .deleted(false) // 가입 시 deleted = false default
                .build();
        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    public UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional
    public void changeUserMode(Long userId, String mode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setCurrentMode(User.Mode.valueOf(mode.toUpperCase()));
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setCurrentMode(user.getCurrentMode().name());
        resp.setDeleted(user.getDeleted());
        return resp;
    }

}
