package com.seeat.seeatapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-definition")
                .pathsToMatch("/api/**") // /api/로 시작하는 모든 요청 스캔
                .build();
    }

    @Bean
    public OpenAPI seeatOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SEEAT API")
                        .description("수산물 거래 플랫폼 SEEAT의 REST API 명세")
                        .version("v2.1"));
    }
}