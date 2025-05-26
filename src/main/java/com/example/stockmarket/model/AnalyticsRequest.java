package com.example.stockmarket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequest {
    private List<String> symbols;
    private Integer movingAveragePeriod = 5;
    private Integer volatilityPeriod = 5;
    private Integer shortMAPeriod = 5;
    private Integer longMAPeriod = 20;
}