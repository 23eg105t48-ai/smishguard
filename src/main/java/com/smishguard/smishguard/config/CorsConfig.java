package com.smishguard.smishguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Global CORS configuration – allowed origins come from the ALLOWED_ORIGINS env var. */
@Configuration
public class CorsConfig {

    @Value("${app.allowed-origins:http://localhost:5173,http://localhost:5174,https://23eg105t48-ai.github.io}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
    }
}
