package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import java.util.List;
import java.util.concurrent.Future;

public interface StockService {
    Future<StockData> fetchStockData(String symbol);
    List<Double> calculateMovingAverage(StockData data, int period);
}
