package com.example.stockmarket.util;

import com.example.stockmarket.model.StockData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ExcelExportUtil {

    private ExcelExportUtil() {
        // Prevent instantiation
    }

    public static void writeStockDataToExcel(Map<String, StockData> data, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock Analysis");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Symbol");
            header.createCell(1).setCellValue("Latest Price");
            header.createCell(2).setCellValue("EMA");
            header.createCell(3).setCellValue("RSI");
            header.createCell(4).setCellValue("Moving Average");
            header.createCell(5).setCellValue("Volatility");
            header.createCell(6).setCellValue("Bollinger Upper");
            header.createCell(7).setCellValue("Bollinger Lower");
            header.createCell(8).setCellValue("MACD");
            header.createCell(9).setCellValue("MACD Signal");
            header.createCell(10).setCellValue("Status Message");
            int rowIdx = 1;
            for (StockData stock : data.values()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stock.getSymbol());
                row.createCell(1).setCellValue(stock.getLatestPrice() != null ? stock.getLatestPrice() : 0);
                row.createCell(2).setCellValue(stock.getEma() != null ? stock.getEma() : 0);
                row.createCell(3).setCellValue(stock.getRsi() != null ? stock.getRsi() : 0);
                row.createCell(4).setCellValue(stock.getMovingAverage() != null ? stock.getMovingAverage() : 0);
                row.createCell(5).setCellValue(stock.getVolatility() != null ? stock.getVolatility() : 0);
                row.createCell(6).setCellValue(stock.getBollingerUpper() != null ? stock.getBollingerUpper() : 0);
                row.createCell(7).setCellValue(stock.getBollingerLower() != null ? stock.getBollingerLower() : 0);
                row.createCell(8).setCellValue(stock.getMacd() != null ? stock.getMacd() : 0);
                row.createCell(9).setCellValue(stock.getMacdSignal() != null ? stock.getMacdSignal() : 0);
                row.createCell(10).setCellValue(stock.getStatusMessage() != null ? stock.getStatusMessage() : "");
            }
            workbook.write(out);
        }
    }
}
