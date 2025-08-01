package com.example.farm4u.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY_RAW;

    @Value("${jwt.expirationMs}")
    private long EXPIRATION_MS;

    // secretKey는 lazy-initialize
    private SecretKey secretKey() {
        // secret이 base64로 관리되고 있음을 가정
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY_RAW));
    }

    /** Access Token 생성 메소드 */
    public String createToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 토큰 유효성 검증 (서명, 만료, 포맷 등) */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 토큰에서 사용자 ID 추출 */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    /** 토큰 만료까지 남은 초 반환 (Blacklist 용 등) */
    public long getRemainExpireSeconds(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        long expirationMillis = claims.getExpiration().getTime();
        return (expirationMillis - System.currentTimeMillis()) / 1000;
    }

    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token); // 이미 구현된 메소드 사용
        // 추가적으로 사용자 정보(UserDetails) 또는 권한 리스트를 가져올 수도 있음
        // 우선은 심플하게 userId만 principal로 사용
        return new UsernamePasswordAuthenticationToken(
                userId,           // principal(유저 PK 또는 UserDetails 객체)
                null,             // credentials(비번 null)
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
