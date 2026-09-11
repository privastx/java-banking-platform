package com.example.bankingplatform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String apiBasePath = "/api";

        Info info = new Info()
                .title("Banking Platform API")
                .version("1.0.0")
                .description("REST API for the Banking Platform application")
                .contact(new Contact().name("Banking Platform Team"));

        return new OpenAPI()
                .info(info);
    }
}