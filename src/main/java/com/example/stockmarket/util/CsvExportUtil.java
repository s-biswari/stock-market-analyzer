package com.example.stockmarket.util;

import com.example.stockmarket.model.StockData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CsvExportUtil {

    // Private constructor to prevent instantiation
    private CsvExportUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void writeStockDataToCsv(Map<String, StockData> data, PrintWriter writer) throws IOException {
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("Symbol", "Latest Price", "Moving Average", "Volatility", "Status Message")
                .build())) {
            for (StockData stock : data.values()) {
                csvPrinter.printRecord(
                        stock.getSymbol(),
                        stock.getLatestPrice(),
                        stock.getMovingAverage(),
                        stock.getVolatility(),
                        stock.getStatusMessage()
                );
            }
        }
    }
}
