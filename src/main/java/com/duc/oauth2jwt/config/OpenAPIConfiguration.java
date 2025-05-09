package com.duc.oauth2jwt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OpenAPIConfiguration implements WebMvcConfigurer {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("OAuth2 JWT")
                        .version("1.0")
                        .description("This is the API documentation for my OAuth2 JWT application."));
    }
}
