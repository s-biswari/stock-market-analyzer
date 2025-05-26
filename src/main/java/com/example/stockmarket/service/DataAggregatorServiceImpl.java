package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DataAggregatorServiceImpl implements DataAggregatorService {
    private final StockService stockService;

    @Override
    public Map<String, StockData> fetchAndAggregate(List<String> symbols) {
        Map<String, Future<StockData>> futures = new ConcurrentHashMap<>();
        Map<String, StockData> results = new ConcurrentHashMap<>();
        for (String symbol : symbols) {
            futures.put(symbol, stockService.fetchStockData(symbol));
        }
        futures.forEach((symbol, future) -> {
            try {
                StockData data = future.get(30, TimeUnit.SECONDS);
                if (data.getClosingPrices() == null || data.getClosingPrices().isEmpty()) {
                    data.setStatusMessage("No data returned from API or symbol not found.");
                    results.put(symbol, data);
                    return;
                }
                // Calculate analytics
                List<Double> ma = stockService.calculateMovingAverage(data, 5);
                if (!ma.isEmpty()) data.setMovingAverage(ma.get(ma.size() - 1));
                if (data.getClosingPrices() != null && data.getClosingPrices().size() >= 5 && stockService instanceof StockServiceImpl stockServiceImpl) {
                    Double volatility = stockServiceImpl.calculateVolatility(data, 5);
                    data.setVolatility(volatility);
                    double finalValue = stockServiceImpl.simulateSimpleMovingAverageStrategy(data, 5, 20);
                    data.setStatusMessage("Simulated final portfolio value: " + finalValue);
                }
                results.put(symbol, data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Interrupted: " + e.getMessage());
                results.put(symbol, errorData);
            } catch (Exception e) {
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Error: " + e.getMessage());
                results.put(symbol, errorData);
            }
        });
        return results;
    }

    @Override
    public Map<String, StockData> fetchAndAggregate(List<String> symbols, int movingAveragePeriod, int volatilityPeriod, int shortMAPeriod, int longMAPeriod) {
        Map<String, Future<StockData>> futures = new ConcurrentHashMap<>();
        Map<String, StockData> results = new ConcurrentHashMap<>();
        for (String symbol : symbols) {
            futures.put(symbol, stockService.fetchStockData(symbol));
        }
        futures.forEach((symbol, future) -> {
            try {
                StockData data = future.get(30, TimeUnit.SECONDS);
                if (data.getClosingPrices() == null || data.getClosingPrices().isEmpty()) {
                    data.setStatusMessage("No data returned from API or symbol not found.");
                    results.put(symbol, data);
                    return;
                }
                // Calculate analytics with custom periods
                List<Double> ma = stockService.calculateMovingAverage(data, movingAveragePeriod);
                if (!ma.isEmpty()) data.setMovingAverage(ma.get(ma.size() - 1));
                if (data.getClosingPrices().size() >= Math.max(movingAveragePeriod, volatilityPeriod)
                    && stockService instanceof StockServiceImpl stockServiceImpl) {
                    Double volatility = stockServiceImpl.calculateVolatility(data, volatilityPeriod);
                    data.setVolatility(volatility);
                    double finalValue = stockServiceImpl.simulateSimpleMovingAverageStrategy(data, shortMAPeriod, longMAPeriod);
                    data.setStatusMessage("Simulated final portfolio value: " + finalValue);
                    // Calculate EMA and RSI
                    Double ema = stockServiceImpl.calculateEMA(data, movingAveragePeriod);
                    data.setEma(ema);
                    Double rsi = stockServiceImpl.calculateRSI(data, movingAveragePeriod);
                    data.setRsi(rsi);
                }
                results.put(symbol, data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Interrupted: " + e.getMessage());
                results.put(symbol, errorData);
            } catch (Exception e) {
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Error: " + e.getMessage());
                results.put(symbol, errorData);
            }
        });
        return results;
    }

    @Override
    public Map<String, StockData> fetchAndAggregateWithDateRange(List<String> symbols, int movingAveragePeriod, int volatilityPeriod, int shortMAPeriod, int longMAPeriod, LocalDate startDate, LocalDate endDate) {
        Map<String, Future<StockData>> futures = new ConcurrentHashMap<>();
        Map<String, StockData> results = new ConcurrentHashMap<>();
        for (String symbol : symbols) {
            futures.put(symbol, stockService.fetchStockData(symbol));
        }
        futures.forEach((symbol, future) -> {
            try {
                StockData data = future.get(30, TimeUnit.SECONDS);
                if (data.getClosingPrices() == null || data.getClosingPrices().isEmpty()) {
                    data.setStatusMessage("No data returned from API or symbol not found.");
                    results.put(symbol, data);
                    return;
                }
                // Filter closing prices by date range if provided
                if (startDate != null && endDate != null) {
                    Map<LocalDate, Double> filtered = new TreeMap<>();
                    data.getClosingPrices().forEach((date, price) -> {
                        if ((date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate))) {
                            filtered.put(date, price);
                        }
                    });
                    data.setClosingPrices(filtered);
                    // Add user-friendly message if no data in range
                    if (filtered.isEmpty()) {
                        data.setStatusMessage("No data available for the selected date range.");
                        results.put(symbol, data);
                        return;
                    }
                }
                // Calculate analytics with custom periods
                List<Double> ma = stockService.calculateMovingAverage(data, movingAveragePeriod);
                if (!ma.isEmpty()) data.setMovingAverage(ma.get(ma.size() - 1));
                if (data.getClosingPrices().size() >= Math.max(movingAveragePeriod, volatilityPeriod)
                    && stockService instanceof StockServiceImpl stockServiceImpl) {
                    Double volatility = stockServiceImpl.calculateVolatility(data, volatilityPeriod);
                    data.setVolatility(volatility);
                    double finalValue = stockServiceImpl.simulateSimpleMovingAverageStrategy(data, shortMAPeriod, longMAPeriod);
                    data.setStatusMessage("Simulated final portfolio value: " + finalValue);
                    // Calculate EMA and RSI
                    Double ema = stockServiceImpl.calculateEMA(data, movingAveragePeriod);
                    data.setEma(ema);
                    Double rsi = stockServiceImpl.calculateRSI(data, movingAveragePeriod);
                    data.setRsi(rsi);
                }
                results.put(symbol, data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Interrupted: " + e.getMessage());
                results.put(symbol, errorData);
            } catch (Exception e) {
                StockData errorData = new StockData();
                errorData.setSymbol(symbol);
                errorData.setStatusMessage("Error: " + e.getMessage());
                results.put(symbol, errorData);
            }
        });
        return results;
    }
}
