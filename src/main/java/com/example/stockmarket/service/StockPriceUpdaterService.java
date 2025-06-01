package com.example.stockmarket.service;

import com.example.stockmarket.model.PortfolioStock;
import com.example.stockmarket.repository.PortfolioStockRepository;
import com.example.stockmarket.config.AlphaVantageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockPriceUpdaterService {
    private static final Logger logger = LoggerFactory.getLogger(StockPriceUpdaterService.class);
    private final PortfolioStockRepository portfolioStockRepository;
    private final RestTemplate restTemplate;
    private final AlphaVantageConfig config;

    // Cache to store stock prices temporarily
    private final Map<String, Double> priceCache = new HashMap<>();

    public StockPriceUpdaterService(PortfolioStockRepository portfolioStockRepository, RestTemplate restTemplate, AlphaVantageConfig config) {
        this.portfolioStockRepository = portfolioStockRepository;
        this.restTemplate = restTemplate;
        this.config = config;
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

    @Scheduled(cron = "0 */2 * * * ?")
    public void updateStockPrices() {
        List<PortfolioStock> stocks = portfolioStockRepository.findAll();
        for (PortfolioStock stock : stocks) {
            try {
                String symbol = stock.getSymbol();

                // Check cache for the stock price
                Double cachedPrice = priceCache.get(symbol);
                if (cachedPrice != null) {
                    stock.setCurrentPrice(cachedPrice);
                    portfolioStockRepository.save(stock);
                    logger.info("Updated current price for {} from cache: {}", symbol, cachedPrice);
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

                        // Update cache
                        priceCache.put(symbol, currentPrice);
                        logger.info("Updated current price for {}: {}", symbol, currentPrice);
                    }
                } else {
                    logger.warn("No valid data found for stock symbol: {}", symbol);
                }
            } catch (Exception e) {
                logger.error("Failed to update price for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }

        // Clear cache after update
        priceCache.clear();
    }
}
