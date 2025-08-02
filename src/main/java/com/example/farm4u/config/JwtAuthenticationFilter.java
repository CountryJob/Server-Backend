package com.example.farm4u.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request); // 헤더에서 Bearer 토큰 추출

        System.out.println("토큰: " + token);
        System.out.println("토큰 유효성: " + jwtProvider.validateToken(token));

        if (token != null && jwtProvider.validateToken(token)) { // 유효성 체크
            Authentication auth = jwtProvider.getAuthentication(token); // 유저 인증 객체 생성

            SecurityContextHolder.getContext().setAuthentication(auth); // SecurityContextHolder에 저장
        }
        filterChain.doFilter(request, response); // 다음 필터로 이동
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
