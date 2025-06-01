package com.example.stockmarket.service;

import com.example.stockmarket.config.AlphaVantageConfig;
import com.example.stockmarket.model.Portfolio;
import com.example.stockmarket.model.PortfolioAnalyticsDTO;
import com.example.stockmarket.model.PortfolioStock;
import com.example.stockmarket.repository.PortfolioRepository;
import com.example.stockmarket.repository.PortfolioStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final AlphaVantageConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PORTFOLIO_NOT_FOUND = "Portfolio not found";

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository, PortfolioStockRepository portfolioStockRepository, AlphaVantageConfig config) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioStockRepository = portfolioStockRepository;
        this.config = config;
    }

    @Override
    public Portfolio createPortfolio(String name, String owner) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(name);
        portfolio.setOwner(owner);
        return portfolioRepository.save(portfolio);
    }

    @Override
    public List<Portfolio> getPortfoliosByOwner(String owner) {
        return portfolioRepository.findByOwner(owner);
    }

    @Override
    public Optional<Portfolio> getPortfolio(Long id) {
        return portfolioRepository.findById(id);
    }

    @Override
    public void deletePortfolio(Long id) {
        portfolioRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PortfolioStock addStockToPortfolio(Long portfolioId, String symbol, Integer quantity, Double buyPrice) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException(PORTFOLIO_NOT_FOUND));
        PortfolioStock stock = new PortfolioStock();
        stock.setPortfolio(portfolio);
        stock.setSymbol(symbol);
        stock.setQuantity(quantity);
        stock.setBuyPrice(buyPrice);

        // Fetch current price using Alpha Vantage API
        Double currentPrice = fetchCurrentPrice(symbol);
        stock.setCurrentPrice(currentPrice);

        return portfolioStockRepository.save(stock);
    }

    private Double fetchCurrentPrice(String symbol) {
        try {
            String apiKey = config.getApiKey(); // Replace with dynamic retrieval if needed
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Object globalQuoteObj = response.get("Global Quote");
            if (globalQuoteObj instanceof Map<?, ?> globalQuote) {
                Object priceObj = globalQuote.get("05. price");
                if (priceObj instanceof String price) {
                    return Double.valueOf(price);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch current price for symbol {}: {}", symbol, e.getMessage());
        }
        return null; // Handle fallback logic if needed
    }

    @Override
    public void removeStockFromPortfolio(Long portfolioStockId) {
        portfolioStockRepository.deleteById(portfolioStockId);
    }

    @Override
    public List<PortfolioStock> getStocksInPortfolio(Long portfolioId) {
        return portfolioStockRepository.findByPortfolioId(portfolioId);
    }

    @Override
    public Portfolio updatePortfolioAnalytics(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
        List<PortfolioStock> stocks = portfolioStockRepository.findByPortfolioId(portfolioId);
        double totalValue = 0.0;
        double totalCost = 0.0;
        for (PortfolioStock stock : stocks) {
            double currentPrice = getSafePrice(stock.getCurrentPrice(), stock.getBuyPrice());
            double qty = getSafeQuantity(stock.getQuantity());
            totalValue += currentPrice * qty;
            totalCost += getSafePrice(stock.getBuyPrice(), 0.0) * qty;
        }
        double pnl = totalValue - totalCost;
        logPortfolioSummary(totalValue, totalCost, pnl);
        logStockAllocations(stocks, totalValue);
        return portfolio;
    }

    @Override
    public PortfolioAnalyticsDTO getPortfolioAnalytics(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException(PORTFOLIO_NOT_FOUND));
        List<PortfolioStock> stocks = portfolioStockRepository.findByPortfolioId(portfolioId);

        double totalValue = stocks.stream()
                .mapToDouble(stock -> getSafePrice(stock.getCurrentPrice(), stock.getBuyPrice()) * getSafeQuantity(stock.getQuantity()))
                .sum();

        double totalCost = stocks.stream()
                .mapToDouble(stock -> getSafePrice(stock.getBuyPrice(), 0.0) * getSafeQuantity(stock.getQuantity()))
                .sum();

        double pnl = totalValue - totalCost;

        List<PortfolioAnalyticsDTO.StockAllocation> allocations = new ArrayList<>();
        for (PortfolioStock stock : stocks) {
            String symbol = stock.getSymbol();
            if (symbol != null && !symbol.isEmpty()) {
                double currentPrice = getSafePrice(stock.getCurrentPrice(), stock.getBuyPrice());
                double qty = getSafeQuantity(stock.getQuantity());
                double positionValue = currentPrice * qty;
                double allocation = totalValue > 0 ? (positionValue / totalValue) * 100 : 0;
                PortfolioAnalyticsDTO.StockAllocation alloc = new PortfolioAnalyticsDTO.StockAllocation();
                alloc.setSymbol(symbol);
                alloc.setAllocation(allocation);
                alloc.setPositionValue(positionValue);
                alloc.setBuyPrice(stock.getBuyPrice() != null ? stock.getBuyPrice() : 0.0);
                alloc.setCurrentPrice(currentPrice);
                alloc.setQuantity(qty > 0 ? (int) qty : 0);
                allocations.add(alloc);
            }
        }

        PortfolioAnalyticsDTO dto = new PortfolioAnalyticsDTO();
        dto.setPortfolioId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setOwner(portfolio.getOwner());
        dto.setTotalValue(totalValue);
        dto.setTotalCost(totalCost);
        dto.setPnl(pnl);
        dto.setAllocations(allocations);
        return dto;
    }

    private double getSafePrice(Double price, Double fallback) {
        if (price != null) return price;
        if (fallback != null) return fallback;
        return 0.0;
    }

    private double getSafeQuantity(Integer quantity) {
        return quantity != null ? quantity : 0.0;
    }

    private void logPortfolioSummary(double totalValue, double totalCost, double pnl) {
        logger.info("Portfolio Value: {}", totalValue);
        logger.info("Portfolio Cost: {}", totalCost);
        logger.info("Portfolio P&L: {}", pnl);
    }

    private void logStockAllocations(List<PortfolioStock> stocks, double totalValue) {
        for (PortfolioStock stock : stocks) {
            String symbol = stock.getSymbol();
            if (symbol != null && !symbol.isEmpty()) {
                double currentPrice = getSafePrice(stock.getCurrentPrice(), stock.getBuyPrice());
                double qty = getSafeQuantity(stock.getQuantity());
                double positionValue = currentPrice * qty;
                double allocation = totalValue > 0 ? (positionValue / totalValue) * 100 : 0;
                String allocationStr = String.format("%.2f", allocation);
                logger.info("Symbol: {}, Allocation: {}%", symbol, allocationStr);
            }
        }
    }

    @Override
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }
}
