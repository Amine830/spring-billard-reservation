package com.amine.billardbook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe de configuration principale.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://editor-next.swagger.io", 
                    "http://localhost", 
                    "http://127.0.0.1", 
                    "http://localhost:8080",
                    "http://127.0.0.1:8080",
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://localhost:4173",
                    "http://127.0.0.1:4173",
                    "http://localhost:5500",
                    "http://127.0.0.1:5500",
                    "https://billard-book.netlify.app"
                    )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("Authorization", "Content-Type", "Location", "Link", "Set-Cookie")
                .allowCredentials(true).maxAge(3600);
    }
}
