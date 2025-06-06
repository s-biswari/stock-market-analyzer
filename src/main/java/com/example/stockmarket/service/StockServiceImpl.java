package com.example.stockmarket.service;

import com.example.stockmarket.model.StockData;
import com.example.stockmarket.config.AlphaVantageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final AlphaVantageConfig config;
    private final ExecutorService executorService;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    private static final String ERROR_MESSAGE_KEY = "Error Message";
    private static final String NOTE_KEY = "Note";
    private static final String TIME_SERIES_KEY_PREFIX = "Time Series";

    @Override
    public Future<StockData> fetchStockData(String symbol) {
        log.info("Fetching stock data for symbol: {}", symbol);
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
                log.error("No response from Alpha Vantage API for symbol: {}", symbol);
                return errorStockData(symbol, "No response from Alpha Vantage API");
            }
            if (response.containsKey(NOTE_KEY)) {
                log.warn("API rate limit reached for symbol: {}. Note: {}", symbol, response.get(NOTE_KEY));
                return errorStockData(symbol, "API rate limit reached: " + response.get(NOTE_KEY));
            }
            if (response.containsKey(ERROR_MESSAGE_KEY)) {
                log.error("API error for symbol: {}. Error Message: {}", symbol, response.get(ERROR_MESSAGE_KEY));
                return errorStockData(symbol, "API error: " + response.get(ERROR_MESSAGE_KEY));
            }
            // Find the time series key dynamically
            String timeSeriesKey = response.keySet().stream()
                .filter(k -> k.contains(TIME_SERIES_KEY_PREFIX))
                .findFirst().orElse(null);
            if (timeSeriesKey != null) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response.get(timeSeriesKey);
                // Sort dates descending (most recent first)
                List<String> sortedDates = new ArrayList<>(timeSeries.keySet());
                sortedDates.sort(Comparator.reverseOrder());
                int count = 0;
                for (String dateStr : sortedDates) {
                    LocalDate date = parseDateSafe(dateStr);
                    Double close = parseDoubleSafe(timeSeries.get(dateStr).get("4. close"));
                    if (date != null && close != null) {
                        prices.put(date, close);
                        count++;
                    }
                    if (count >= 30) break; // Limit to most recent 30 valid days
                }
                log.info("Successfully fetched stock data for symbol: {}", symbol);
                return new StockData(symbol, prices);
            } else {
                log.error("Unexpected API response structure for symbol: {}. Response keys: {}", symbol, response.keySet());
                return errorStockData(symbol, "Unexpected API response structure: " + response.keySet());
            }
        } catch (Exception e) {
            log.error("Exception while fetching stock data for symbol: {}. Exception: {}", symbol, e.getMessage());
            return errorStockData(symbol, "Exception: " + e.getMessage());
        }
    }

    private LocalDate parseDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private Double parseDoubleSafe(String doubleStr) {
        try {
            return Double.parseDouble(doubleStr);
        } catch (Exception e) {
            log.warn("Failed to parse double: {}", doubleStr);
            return null;
        }
    }

    private StockData errorStockData(String symbol, String message) {
        log.error("Error fetching stock data for symbol: {}. Message: {}", symbol, message);
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

    public Double calculateEMA(StockData data, int period) {
        List<Double> prices = new ArrayList<>(data.getClosingPrices().values());
        if (prices.size() < period) return null;
        double multiplier = 2.0 / (period + 1);
        double ema = prices.get(0);
        for (int i = 1; i < prices.size(); i++) {
            ema = ((prices.get(i) - ema) * multiplier) + ema;
        }
        return ema;
    }

    public Double calculateRSI(StockData data, int period) {
        List<Double> prices = new ArrayList<>(data.getClosingPrices().values());
        if (prices.size() <= period) return null;
        double gain = 0;
        double loss = 0;
        for (int i = 1; i <= period; i++) {
            double diff = prices.get(i) - prices.get(i - 1);
            if (diff >= 0) gain += diff;
            else loss -= diff;
        }
        gain /= period;
        loss /= period;
        for (int i = period + 1; i < prices.size(); i++) {
            double diff = prices.get(i) - prices.get(i - 1);
            if (diff >= 0) {
                gain = (gain * (period - 1) + diff) / period;
                loss = (loss * (period - 1)) / period;
            } else {
                gain = (gain * (period - 1)) / period;
                loss = (loss * (period - 1) - diff) / period;
            }
        }
        if (loss == 0) return 100.0;
        double rs = gain / loss;
        return 100 - (100 / (1 + rs));
    }

    public List<Double> calculateBollingerUpper(StockData data, int period, double numStdDev) {
        List<Double> upperBand = new ArrayList<>();
        List<Double> closes = new ArrayList<>(data.getClosingPrices().values());
        for (int i = 0; i <= closes.size() - period; i++) {
            List<Double> window = closes.subList(i, i + period);
            double mean = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double std = Math.sqrt(window.stream().mapToDouble(p -> Math.pow(p - mean, 2)).sum() / period);
            upperBand.add(mean + numStdDev * std);
        }
        return upperBand;
    }

    public List<Double> calculateBollingerLower(StockData data, int period, double numStdDev) {
        List<Double> lowerBand = new ArrayList<>();
        List<Double> closes = new ArrayList<>(data.getClosingPrices().values());
        for (int i = 0; i <= closes.size() - period; i++) {
            List<Double> window = closes.subList(i, i + period);
            double mean = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double std = Math.sqrt(window.stream().mapToDouble(p -> Math.pow(p - mean, 2)).sum() / period);
            lowerBand.add(mean - numStdDev * std);
        }
        return lowerBand;
    }

    public List<Double> calculateMACD(StockData data, int shortPeriod, int longPeriod) {
        List<Double> closes = new ArrayList<>(data.getClosingPrices().values());
        List<Double> macd = new ArrayList<>();
        List<Double> shortEma = calculateEMAList(closes, shortPeriod);
        List<Double> longEma = calculateEMAList(closes, longPeriod);
        for (int i = 0; i < closes.size(); i++) {
            if (i < longEma.size()) {
                macd.add(shortEma.get(i) - longEma.get(i));
            } else {
                macd.add(0.0);
            }
        }
        return macd;
    }

    public List<Double> calculateMACDSignal(List<Double> macdLine, int signalPeriod) {
        return calculateEMAList(macdLine, signalPeriod);
    }

    private List<Double> calculateEMAList(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        for (int i = 0; i < prices.size(); i++) {
            if (i == 0) {
                ema.add(prices.get(i));
            } else {
                ema.add((prices.get(i) - ema.get(i - 1)) * multiplier + ema.get(i - 1));
            }
        }
        return ema;
    }
}
