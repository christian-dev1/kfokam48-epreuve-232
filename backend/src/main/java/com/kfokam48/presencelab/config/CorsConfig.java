package com.kfokam48.presencelab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Autorise le frontend (Vite, port 5173 par défaut) à appeler l'API. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${presencelab.cors.origines:http://localhost:5173}")
    private String[] origines;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origines)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS");
    }
}
