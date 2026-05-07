package com.korebit.rigel.config;

import com.korebit.rigel.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;

    public WebConfig(LogInterceptor logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-Correlation-Id")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        "/actuator/**",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.map",
                        "/**/*.ico",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.svg"
                );
    }
}