package com.jumunhasyeotjo.order_to_shipping.common.config;

import com.library.passport.proto.PassportProto;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(PassportProto.Passport.class);
    }

    @Bean
    public OpenAPI openAPI() {
        // 1. Security Scheme 정의 (설정 정의)
        String jwtSchemeName = "bearerAuth";
        String passportSchemeName = "passportHeader";

        // JWT (Bearer Token) 설정
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Passport (Custom Header) 설정 - 핵심 부분
        SecurityScheme passportHeader = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Passport") // 실제 헤더 키 값 (Gateway와 일치시켜야 함)
                .description("Gateway에서 전달받는 사용자 정보 (JSON String)");

        // 2. Security Requirement 정의 (전역 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName)
                .addList(passportSchemeName);

        // 3. OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .info(new Info()
                        .title("Order-Shipping API")
                        .description("주문-배송 API 문서")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, bearerAuth)
                        .addSecuritySchemes(passportSchemeName, passportHeader))
                .addSecurityItem(securityRequirement);
    }
}