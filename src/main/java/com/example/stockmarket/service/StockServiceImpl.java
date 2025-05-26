package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import com.example.stockmarket.config.AlphaVantageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final AlphaVantageConfig config;
    private final ExecutorService executorService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Future<StockData> fetchStockData(String symbol) {
        return executorService.submit(() -> fetchStockDataInternal(symbol));
    }

    private StockData fetchStockDataInternal(String symbol) {
        String apiKey = config.getApiKey();
        String url = String.format(
            "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
            symbol, apiKey
        );
        try {
            Map<LocalDate, Double> prices = new TreeMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return errorStockData(symbol, "No response from Alpha Vantage API");
            }
            if (response.containsKey("Note")) {
                return errorStockData(symbol, "API rate limit reached: " + response.get("Note"));
            }
            if (response.containsKey("Error Message")) {
                return errorStockData(symbol, "API error: " + response.get("Error Message"));
            }
            if (response.containsKey("Time Series (Daily)")) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response.get("Time Series (Daily)");
                int count = 0;
                for (Map.Entry<String, Map<String, String>> entry : timeSeries.entrySet()) {
                    if (count++ >= 30) break; // Limit to last 30 days
                    LocalDate date = parseDateSafe(entry.getKey());
                    Double close = parseDoubleSafe(entry.getValue().get("4. close"));
                    if (date != null && close != null) {
                        prices.put(date, close);
                    }
                }
                return new StockData(symbol, prices);
            } else {
                return errorStockData(symbol, "Unexpected API response structure");
            }
        } catch (Exception e) {
            return errorStockData(symbol, "Exception: " + e.getMessage());
        }
    }

    private LocalDate parseDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String doubleStr) {
        try {
            return Double.parseDouble(doubleStr);
        } catch (Exception e) {
            return null;
        }
    }

    private StockData errorStockData(String symbol, String message) {
        StockData errorData = new StockData();
        errorData.setSymbol(symbol);
        errorData.setStatusMessage(message);
        return errorData;
    }

    @Override
    public List<Double> calculateMovingAverage(StockData data, int period) {
        List<Double> prices = new ArrayList<>(data.getClosingPrices().values());
        List<Double> movingAverages = new ArrayList<>();
        for (int i = 0; i <= prices.size() - period; i++) {
            double sum = 0;
            for (int j = i; j < i + period; j++) {
                sum += prices.get(j);
            }
            movingAverages.add(sum / period);
        }
        return movingAverages;
    }

    public Double calculateVolatility(StockData data, int period) {
        List<Double> prices = new ArrayList<>(data.getClosingPrices().values());
        if (prices.size() < period) return null;
        List<Double> subList = prices.subList(prices.size() - period, prices.size());
        double mean = subList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = subList.stream().mapToDouble(p -> Math.pow(p - mean, 2)).sum() / period;
        return Math.sqrt(variance);
    }

    public double simulateSimpleMovingAverageStrategy(StockData data, int shortPeriod, int longPeriod) {
        List<Double> prices = new ArrayList<>(data.getClosingPrices().values());
        if (prices.size() < longPeriod) return 0.0;
        double cash = 10000.0;
        double shares = 0.0;
        for (int i = longPeriod; i < prices.size(); i++) {
            double shortMA = prices.subList(i - shortPeriod, i).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double longMA = prices.subList(i - longPeriod, i).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double price = prices.get(i);
            if (shortMA > longMA && cash >= price) {
                shares = cash / price;
                cash = 0;
            } else if (shortMA < longMA && shares > 0) {
                cash = shares * price;
                shares = 0;
            }
        }
        // Liquidate at last price
        if (shares > 0) {
            cash = shares * prices.get(prices.size() - 1);
        }
        return cash;
    }
}
