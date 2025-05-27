package com.example.stockmarket.model;

import lombok.Data;

@Data
public class AddStockRequest {
    private String symbol;
    private Integer quantity;
    private Double buyPrice;
}
