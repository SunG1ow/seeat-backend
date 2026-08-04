package com.seeat.seeatapi.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");
        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}