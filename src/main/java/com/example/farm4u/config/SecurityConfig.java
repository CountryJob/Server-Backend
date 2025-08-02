package com.example.farm4u.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)                                              // REST API - CSRF 불필요
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))         // 세션 사용 안 함(JWT만)
                .authorizeHttpRequests(auth ->
//                        // 비로그인 허용 API
                        auth.requestMatchers("/api/v0/auth/request-code", "/api/v0/auth/verify-code", "/error").permitAll()
                                .requestMatchers("/api/v0/users/**", "/api/v0/farmers/**", "/api/v0/farmers/list",
                                        "/api/v0/workers/**", "/api/v0/workers/list",
                                        "/api/v0/notifications/internal", "/api/v0/reports").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()   // 문서/테스트 툴도 예외
//                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
//                        auth.anyRequest().permitAll() // 전체 비로그인 허용 (개발 테스트)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // CORS 허용 설정이 필요하다면 .cors() 옵션 추가 설정
        return http.build();
    }
}
