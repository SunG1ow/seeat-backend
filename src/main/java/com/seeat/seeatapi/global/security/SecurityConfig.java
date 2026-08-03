package com.seeat.seeatapi.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 인증 불필요
    private static final String[] PERMIT_ALL_PATHS = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/admin/login",
            "/api/v1/admin/otp/setup",
            "/api/v1/admin/otp/verify",
            "/api/v1/products/categories",
            "/api/v1/products/search",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/**"
    };

    // 판매자(ROLE_SELLER) 전용
    private static final String[] SELLER_ONLY_PATHS = {
            "/api/v1/products",              // POST 2-1 상품등록
            "/api/v1/products/*",            // PUT 2-5 상품수정
            "/api/v1/products/*/images",     // POST 2-6 이미지추가
            "/api/v1/products/*/images/*",   // DELETE 2-7 이미지삭제
            "/api/v1/seller/**"               // 7-1, 7-2 대시보드/정산
    };

    // 관리자(ROLE_ADMIN) 전용
    private static final String[] ADMIN_ONLY_PATHS = {
            "/api/v1/admin/**",                          // 1-5, 1-6 OTP등록, 4-8 환불
            "/api/v1/reports/*/status"                    // 6-2 신고처리
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        // 4-3 주문 상태 변경(PATCH)은 관리자 전용 — order 경로 중 상태변경만 예외적으로 관리자 제한
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasRole("ADMIN")
                        .requestMatchers(SELLER_ONLY_PATHS).hasRole("SELLER")
                        .requestMatchers(ADMIN_ONLY_PATHS).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}