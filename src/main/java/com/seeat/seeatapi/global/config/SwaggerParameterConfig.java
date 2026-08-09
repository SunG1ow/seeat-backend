package com.seeat.seeatapi.global.config;

import com.seeat.seeatapi.global.security.CurrentMemberId;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

@Component
public class SwaggerParameterConfig implements ParameterCustomizer {

    @Override
    public io.swagger.v3.oas.models.parameters.Parameter customize(
            io.swagger.v3.oas.models.parameters.Parameter parameter,
            MethodParameter methodParameter
    ) {
        // @CurrentMemberId는 JWT 토큰에서 서버가 자동으로 채우는 값이라
        // 클라이언트가 입력할 파라미터가 아님 -> Swagger 문서에서 완전히 제외
        if (methodParameter.hasParameterAnnotation(CurrentMemberId.class)) {
            return null;
        }
        return parameter;
    }
}