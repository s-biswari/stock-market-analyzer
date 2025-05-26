package com.example.stockmarket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockData {
    private String symbol;
    private Map<LocalDate, Double> closingPrices;
    private Double latestPrice;
    private Double movingAverage;
    private Double volatility;
    private String statusMessage; // For error/status reporting

    public StockData(String symbol, Map<LocalDate, Double> closingPrices) {
        this.symbol = symbol;
        this.closingPrices = closingPrices;
        if (closingPrices != null && !closingPrices.isEmpty()) {
            this.latestPrice = closingPrices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);
        }
    }
}
