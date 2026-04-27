package com.maplestory.ledger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT(JSON Web Token) 생성 및 검증 컴포넌트.
 *
 * 인증 흐름:
 *   1. 로그인/회원가입 성공 → generateToken()으로 JWT 발급
 *   2. 클라이언트는 이후 요청 헤더에 "Authorization: Bearer {token}" 포함
 *   3. JwtAuthFilter에서 validateToken()으로 유효성 검사 후 getUserId()로 사용자 식별
 *
 * 사용 라이브러리: JJWT 0.12.x (io.jsonwebtoken)
 * 서명 알고리즘: HMAC-SHA256 (HS256)
 * 토큰 유효기간: application.yml의 jwt.expiration 값 (기본 7일)
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String nickname) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("nickname", nickname)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
