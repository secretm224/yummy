package com.cho_co_song_i.yummy.yummy.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Yummy API Docs")
                        .description("Yummy 프로젝트 Swagger 문서")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                        .contact(new Contact()
                                .name("초코송이 Dev Team")
                                .email("dev@yummy.com")
                                .url("http://secretm-yummy.com/")))
                                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                                .components(new Components()
                                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("yummy-public")
                .pathsToMatch("/**") // 문서화할 엔드포인트 경로
                .build();
    }
}
