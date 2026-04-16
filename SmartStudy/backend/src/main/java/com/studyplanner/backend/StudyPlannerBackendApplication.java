package com.studyplanner.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class StudyPlannerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyPlannerBackendApplication.class, args);
        System.out.println("\n\n");
        System.out.println("===========================================");
        System.out.println("  Smart Study Planner Backend Started!");
        System.out.println("  Running on: http://localhost:8080");
        System.out.println("===========================================");
        System.out.println("\n");
    }

    /**
     * Bean for RestTemplate - used to call Python ML service
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * CORS Configuration - allows frontend to call backend
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                            "http://localhost:3000",
                            "http://localhost:5500",
                            "http://127.0.0.1:5500",
                            "http://localhost:8080"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}