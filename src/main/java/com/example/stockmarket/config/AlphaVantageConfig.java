package com.example.stockmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlphaVantageConfig {
    @Value("${alphavantage.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
