package com.seeat.seeatapi.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    // 일반 회원(구매자/판매자)용 Access Token 발급
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, jwtProperties.getAccessTokenExpiration());
    }

    // 관리자용 Access Token 발급
    public String createAdminAccessToken(Long userId, String role) {
        return createToken(userId, role, jwtProperties.getAdminAccessTokenExpiration());
    }

    // Refresh Token 발급 (role 없이 subject만)
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private String createToken(Long userId, String role, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        // 생성 시 'ROLE_' 접두사 보정 처리 (선택)
        String formattedRole = (role != null && !role.startsWith("ROLE_")) ? "ROLE_" + role : role;

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", formattedRole)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // [신규] 토큰 기반으로 Spring Security Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long userId = Long.valueOf(claims.getSubject());
        List<GrantedAuthority> authorities = getAuthorities(claims);

        // Security Context에 저장할 User Principal 생성
        User principal = new User(String.valueOf(userId), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // [신규] ROLE_ 접두사가 보장된 GrantedAuthority 리스트 추출
    public List<GrantedAuthority> getAuthorities(Claims claims) {
        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }

        // ROLE_ 접두사 누락 방지
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(authority));
    }

    // 토큰에서 userId(subject) 추출
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // 토큰에서 role 클레임 추출
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false; // 만료된 토큰
        } catch (Exception e) {
            return false; // 서명 불일치, 형식 오류 등
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}