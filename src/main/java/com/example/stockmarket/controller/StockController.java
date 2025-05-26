package com.example.stockmarket.controller;

import com.example.stockmarket.model.StockData;
import com.example.stockmarket.service.DataAggregatorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import com.example.stockmarket.util.ExcelExportUtil;
import com.example.stockmarket.util.CsvExportUtil;
import com.example.stockmarket.model.AnalyticsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Stock Analysis", description = "Endpoints for analyzing and exporting stock market data with custom analytics periods.")
@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final DataAggregatorService aggregatorService;

    public StockController(DataAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @Operation(summary = "Analyze stocks and return analytics as JSON", description = "Fetches stock data for the given symbols and returns analytics (moving average, volatility, simulated strategy) using custom periods.")
    @PostMapping("/analyze")
    public Map<String, StockData> analyzeStocks(@RequestBody AnalyticsRequest request) {
        return aggregatorService.fetchAndAggregate(
            request.getSymbols(),
            request.getMovingAveragePeriod(),
            request.getVolatilityPeriod(),
            request.getShortMAPeriod(),
            request.getLongMAPeriod()
        );
    }

    @Operation(summary = "Export stock analytics as CSV", description = "Fetches stock data and analytics for the given symbols and returns the result as a downloadable CSV file. Custom periods can be specified.")
    @PostMapping(value = "/analyze/csv", produces = "text/csv")
    public void analyzeStocksCsv(@RequestBody AnalyticsRequest request, HttpServletResponse response) throws java.io.IOException {
        Map<String, StockData> result = aggregatorService.fetchAndAggregate(
            request.getSymbols(),
            request.getMovingAveragePeriod(),
            request.getVolatilityPeriod(),
            request.getShortMAPeriod(),
            request.getLongMAPeriod()
        );
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=stock-analysis.csv");
        CsvExportUtil.writeStockDataToCsv(result, response.getWriter());
    }

    @Operation(summary = "Export stock analytics as Excel", description = "Fetches stock data and analytics for the given symbols and returns the result as a downloadable Excel (.xlsx) file. Custom periods can be specified.")
    @PostMapping(value = "/analyze/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void analyzeStocksExcel(@RequestBody AnalyticsRequest request, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Map<String, StockData> result = aggregatorService.fetchAndAggregate(
            request.getSymbols(),
            request.getMovingAveragePeriod(),
            request.getVolatilityPeriod(),
            request.getShortMAPeriod(),
            request.getLongMAPeriod()
        );
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stock-analysis.xlsx");
        ExcelExportUtil.writeStockDataToExcel(result, response.getOutputStream());
    }
}
