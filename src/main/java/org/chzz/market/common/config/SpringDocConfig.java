package org.chzz.market.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.chzz.market.common.springdoc.ApiExceptionExplainParser;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * SpringDoc OpenAPI 설정을 정의하는 클래스입니다.
 */
@Configuration
public class SpringDocConfig {


    /**
     * SpringDoc OpenAPI 스펙을 반환하는 Bean을 생성합니다.
     *
     * @return OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Chzz Market API")
                .version("v1");
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))  // JWT 토큰 설정
                .info(info);
    }

    @Bean
    public OperationCustomizer customizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiExceptionExplainParser.parse(operation, handlerMethod); // 예외 설명 파싱
            return operation;
        };
    }

    /**
     * 전역적으로 특정 파라미터(예: @LoginUser)를 Swagger 문서에서 숨기는 설정을 정의합니다.
     *
     * @return OpenApiCustomiser 객체
     */
    @Bean
    public OpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getParameters() != null) {
                        operation.setParameters(
                                operation.getParameters().stream()
                                        .filter(parameter -> !parameter.getName().equals("userId"))  // userId 파라미터 필터링
                                        .toList()
                        );
                    }

                    if (operation.getRequestBody() != null &&
                            operation.getRequestBody().getContent() != null &&
                            operation.getRequestBody().getContent().containsKey("multipart/form-data")) {
                        operation.getRequestBody().getContent().get("multipart/form-data")
                                .getSchema().getProperties().remove("userId");
                    }
                })
        );
    }
}
