package com.example.stockmarket.controller;

import com.example.stockmarket.model.AddStockRequest;
import com.example.stockmarket.model.Portfolio;
import com.example.stockmarket.model.PortfolioAnalyticsDTO;
import com.example.stockmarket.model.PortfolioStock;
import com.example.stockmarket.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody Portfolio portfolio) {
        Portfolio created = portfolioService.createPortfolio(portfolio.getName(), portfolio.getOwner());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Portfolio>> getPortfoliosByOwner(@RequestParam(required = false) String owner) {
        if (owner != null && !owner.isEmpty()) {
            return ResponseEntity.ok(portfolioService.getPortfoliosByOwner(owner));
        } else {
            return ResponseEntity.ok(portfolioService.getAllPortfolios());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable Long id) {
        Optional<Portfolio> portfolio = portfolioService.getPortfolio(id);
        return portfolio.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{portfolioId}/stocks")
    public ResponseEntity<PortfolioStock> addStockToPortfolio(
            @PathVariable Long portfolioId,
            @RequestBody AddStockRequest request) {
        PortfolioStock stock = portfolioService.addStockToPortfolio(
            portfolioId, request.getSymbol(), request.getQuantity(), request.getBuyPrice());
        return ResponseEntity.ok(stock);
    }

    @DeleteMapping("/stocks/{portfolioStockId}")
    public ResponseEntity<Void> removeStockFromPortfolio(@PathVariable Long portfolioStockId) {
        portfolioService.removeStockFromPortfolio(portfolioStockId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioId}/stocks")
    public ResponseEntity<List<PortfolioStock>> getStocksInPortfolio(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(portfolioService.getStocksInPortfolio(portfolioId));
    }

    @PostMapping("/{portfolioId}/analytics")
    public ResponseEntity<PortfolioAnalyticsDTO> getPortfolioAnalytics(@PathVariable Long portfolioId) {
        PortfolioAnalyticsDTO analytics = portfolioService.getPortfolioAnalytics(portfolioId);
        return ResponseEntity.ok(analytics);
    }
}
