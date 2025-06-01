package com.example.stockmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AlphaVantageConfig {
    @Value("${alphavantage.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
