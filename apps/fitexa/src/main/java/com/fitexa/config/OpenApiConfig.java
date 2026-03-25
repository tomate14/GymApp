package com.fitexa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/api/v1/auth/login"
    );

    @Bean
    public OpenAPI fitexaOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Fitexa API")
                .description("Documentacion de servicios del backend de Fitexa.")
                .version("v1")
                .contact(new Contact().name("Fitexa").email("soporte@fitexa.local")))
            .components(new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT obtenido desde /api/v1/auth/login")
                ));
    }

    @Bean
    public GroupedOpenApi fitexaApiGroup(OpenApiCustomizer securedOperationsCustomizer) {
        return GroupedOpenApi.builder()
            .group("fitexa")
            .pathsToMatch("/api/**")
            .addOpenApiCustomizer(securedOperationsCustomizer)
            .build();
    }

    @Bean
    public OpenApiCustomizer securedOperationsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> {
                if (PUBLIC_PATHS.contains(path) || path.startsWith("/api/v1/public/")) {
                    return;
                }

                pathItem.readOperations().forEach(operation ->
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                );
            });
        };
    }
}
