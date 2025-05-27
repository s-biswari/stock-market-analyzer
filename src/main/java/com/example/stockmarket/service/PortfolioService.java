package com.example.stockmarket.service;

import com.example.stockmarket.model.Portfolio;
import com.example.stockmarket.model.PortfolioAnalyticsDTO;
import com.example.stockmarket.model.PortfolioStock;
import java.util.List;
import java.util.Optional;

public interface PortfolioService {
    Portfolio createPortfolio(String name, String owner);
    List<Portfolio> getPortfoliosByOwner(String owner);
    Optional<Portfolio> getPortfolio(Long id);
    void deletePortfolio(Long id);

    PortfolioStock addStockToPortfolio(Long portfolioId, String symbol, Integer quantity, Double buyPrice);
    void removeStockFromPortfolio(Long portfolioStockId);
    List<PortfolioStock> getStocksInPortfolio(Long portfolioId);
    Portfolio updatePortfolioAnalytics(Long portfolioId);
    List<Portfolio> getAllPortfolios();
    PortfolioAnalyticsDTO getPortfolioAnalytics(Long portfolioId);
}
