package com.seeat.seeatapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // 프론트 로컬 개발 서버 (예: React/Next.js)
                "http://localhost:5173",    // Vite 기본 포트도 쓰는 경우 대비
                "https://seeat-frontend.vercel.app/" // 배포 후 추가
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization")); // 응답 헤더로 토큰 내려줄 경우 필요
        config.setAllowCredentials(true); // 쿠키 또는 Authorization 헤더 사용시 true 필수
        config.setMaxAge(3600L); // preflight 캐시 시간(초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}