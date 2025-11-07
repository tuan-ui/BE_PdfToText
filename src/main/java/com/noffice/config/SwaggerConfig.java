package com.noffice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SwaggerConfig {

    private final Environment environment;

    public SwaggerConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        String[] urls = environment.getProperty("swagger.servers", "").split(",");
        for (String url : urls) {
            String[] parts = url.split(";");
            if (parts.length == 2) {
                servers.add(new Server().url(parts[0]).description(parts[1]));
            }
        }

        return new OpenAPI()
                .info(new Info()
                        .title("NOffice API")
                        .version("05-June-2025 13:45")
                        .description("API documentation for NOFFICE-main"))
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}