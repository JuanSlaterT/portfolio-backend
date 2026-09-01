package com.juandiego.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.juandiego.backend.handlers.VisitorRequestInterceptor;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final VisitorRequestInterceptor visitorRequestInterceptor;

    public CorsConfig(VisitorRequestInterceptor visitorRequestInterceptor) {
        this.visitorRequestInterceptor = visitorRequestInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://juancito.me")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("x-missingTime");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitorRequestInterceptor)
                .addPathPatterns("/**");
    }
}
