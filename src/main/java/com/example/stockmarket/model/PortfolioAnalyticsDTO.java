package com.example.stockmarket.model;

import lombok.Data;
import java.util.List;

@Data
public class PortfolioAnalyticsDTO {
    private Long portfolioId;
    private String name;
    private String owner;
    private double totalValue;
    private double totalCost;
    private double pnl;
    private List<StockAllocation> allocations;

    @Data
    public static class StockAllocation {
        private String symbol;
        private double allocation;
        private double positionValue;
        private double buyPrice;
        private double currentPrice;
        private int quantity;
    }
}
