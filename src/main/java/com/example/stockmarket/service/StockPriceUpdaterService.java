package com.example.stockmarket.service;

import com.example.stockmarket.model.PortfolioStock;
import com.example.stockmarket.repository.PortfolioStockRepository;
import com.example.stockmarket.config.AlphaVantageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockPriceUpdaterService {
    private static final Logger logger = LoggerFactory.getLogger(StockPriceUpdaterService.class);
    private final PortfolioStockRepository portfolioStockRepository;
    private final RestTemplate restTemplate;
    private final AlphaVantageConfig config;
    private final RedisTemplate<String, Double> redisTemplate;

    public StockPriceUpdaterService(PortfolioStockRepository portfolioStockRepository, RestTemplate restTemplate, AlphaVantageConfig config, RedisTemplate<String, Double> redisTemplate) {
        this.portfolioStockRepository = portfolioStockRepository;
        this.restTemplate = restTemplate;
        this.config = config;
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchApiResponse(String url) {
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null ? response : new HashMap<>(); // Ensure response is never null
        } catch (Exception e) {
            logger.error("Failed to fetch API response for URL {}: {}", url, e.getMessage());
            return new HashMap<>();
        }
    }

    @Scheduled(cron = "0 30 0 * * ?")
    public void updateStockPrices() {
        List<PortfolioStock> stocks = portfolioStockRepository.findAll();
        for (PortfolioStock stock : stocks) {
            try {
                String symbol = stock.getSymbol();

                // Check Redis cache for the stock price
                Double cachedPrice = redisTemplate.opsForValue().get(symbol);
                if (cachedPrice != null) {
                    stock.setCurrentPrice(cachedPrice);
                    portfolioStockRepository.save(stock);
                    logger.info("Updated current price for {} from Redis cache: {}", symbol, cachedPrice);
                    continue;
                }

                // Fetch price from API if not in cache
                String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + config.getApiKey();
                Map<String, Object> response = fetchApiResponse(url);
                Object globalQuoteObj = response.get("Global Quote");
                if (globalQuoteObj instanceof Map<?, ?> globalQuote) {
                    Object priceObj = globalQuote.get("05. price");
                    if (priceObj instanceof String price) {
                        Double currentPrice = Double.valueOf(price);
                        stock.setCurrentPrice(currentPrice);
                        portfolioStockRepository.save(stock);

                        // Update Redis cache
                        redisTemplate.opsForValue().set(symbol, currentPrice, Duration.ofHours(12));
                        logger.info("Updated current price for {}: {}", symbol, currentPrice);
                    }
                } else {
                    logger.warn("No valid data found for stock symbol: {}", symbol);
                }
            } catch (Exception e) {
                logger.error("Failed to update price for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
    }
}
