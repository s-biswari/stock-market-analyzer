package com.example.stockmarket.controller;

import com.example.stockmarket.model.StockData;
import com.example.stockmarket.service.DataAggregatorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import com.example.stockmarket.util.ExcelExportUtil;
import com.example.stockmarket.util.CsvExportUtil;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final DataAggregatorService aggregatorService;

    public StockController(DataAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @PostMapping("/analyze")
    public Map<String, StockData> analyzeStocks(@RequestBody List<String> symbols) {
        return aggregatorService.fetchAndAggregate(symbols);
    }

    @PostMapping(value = "/analyze/csv", produces = "text/csv")
    public void analyzeStocksCsv(@RequestBody List<String> symbols, HttpServletResponse response) throws java.io.IOException {
        Map<String, StockData> result = aggregatorService.fetchAndAggregate(symbols);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=stock-analysis.csv");
        CsvExportUtil.writeStockDataToCsv(result, response.getWriter());
    }

    @PostMapping(value = "/analyze/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void analyzeStocksExcel(@RequestBody List<String> symbols, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Map<String, StockData> result = aggregatorService.fetchAndAggregate(symbols);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stock-analysis.xlsx");
        ExcelExportUtil.writeStockDataToExcel(result, response.getOutputStream());
    }
}
