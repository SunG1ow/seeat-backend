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
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 모든 HTTP 메서드에 대해 인증 불필요
    private static final String[] PERMIT_ALL_PATHS = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/admin/login",
            "/api/v1/admin/otp/setup",
            "/api/v1/admin/otp/verify",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/actuator/**"
    };

    // 경로 전체에 대해 판매자(ROLE_SELLER) 권한이 필요한 API
    private static final String[] SELLER_ONLY_PATHS = {
            "/api/v1/seller/**" // 7-1, 7-2 대시보드/정산 등
    };

    // 경로 전체에 대해 관리자(ROLE_ADMIN) 권한이 필요한 API
    private static final String[] ADMIN_ONLY_PATHS = {
            "/api/v1/admin/**",       // 1-5, 1-6 OTP등록, 4-8 환불
            "/api/v1/reports/*/status" // 6-2 신고처리
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 공통 전체 허용 경로
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()

                        // 2. 상품 조회(GET): 비로그인/구매자/판매자 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()

                        // 3. 상품 CUD(등록/수정/삭제): 판매자 전용
                        .requestMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/*/images").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/*").hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/*/images/*").hasRole("SELLER")

                        // 4. 주문 상태 변경: 관리자 전용
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasRole("ADMIN")

                        // 5. 역할별 전용 경로
                        .requestMatchers(SELLER_ONLY_PATHS).hasRole("SELLER")
                        .requestMatchers(ADMIN_ONLY_PATHS).hasRole("ADMIN")

                        // 6. 기타 모든 요청: 인증 필요
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