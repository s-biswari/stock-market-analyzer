package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import java.util.List;
import java.util.Map;

public interface DataAggregatorService {
    Map<String, StockData> fetchAndAggregate(List<String> symbols);
}
