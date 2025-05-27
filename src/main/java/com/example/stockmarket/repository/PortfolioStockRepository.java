package com.example.stockmarket.repository;

import com.example.stockmarket.model.PortfolioStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioStockRepository extends JpaRepository<PortfolioStock, Long> {
    List<PortfolioStock> findByPortfolioId(Long portfolioId);
}
