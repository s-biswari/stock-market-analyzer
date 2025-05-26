package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DataAggregatorService {
    Map<String, StockData> fetchAndAggregate(List<String> symbols);

    Map<String, StockData> fetchAndAggregate(List<String> symbols, int movingAveragePeriod, int volatilityPeriod, int shortMAPeriod, int longMAPeriod);

    Map<String, StockData> fetchAndAggregateWithDateRange(List<String> symbols, int movingAveragePeriod, int volatilityPeriod, int shortMAPeriod, int longMAPeriod, LocalDate startDate, LocalDate endDate);
}
