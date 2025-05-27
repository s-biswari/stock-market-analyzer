package com.example.stockmarket.util;

import com.example.stockmarket.model.StockData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Map;

public class PdfExportUtil {
    private PdfExportUtil() {
        // Prevent instantiation
    }

    public static void writeStockDataToPdf(Map<String, StockData> data, ServletOutputStream out) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            float y = page.getMediaBox().getHeight() - 40;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            writeHeader(contentStream, y);
            y -= 20;
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            for (StockData stock : data.values()) {
                if (y < 60) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    y = page.getMediaBox().getHeight() - 40;
                    contentStream = new PDPageContentStream(document, page);
                    writeHeader(contentStream, y);
                    y -= 20;
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                }
                writeStockLine(contentStream, stock, y);
                y -= 15;
            }
            contentStream.close();
            document.save(out);
        }
    }

    private static void writeHeader(PDPageContentStream contentStream, float y) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(40, y);
        contentStream.showText("Symbol | Latest Price | EMA | RSI | Moving Avg | Volatility | Bollinger Upper | Bollinger Lower | MACD | MACD Signal | Status");
        contentStream.endText();
    }

    private static void writeStockLine(PDPageContentStream contentStream, StockData stock, float y) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(40, y);
        String line = String.format("%s | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %s",
            stock.getSymbol(),
            stock.getLatestPrice() != null ? stock.getLatestPrice() : 0,
            stock.getEma() != null ? stock.getEma() : 0,
            stock.getRsi() != null ? stock.getRsi() : 0,
            stock.getMovingAverage() != null ? stock.getMovingAverage() : 0,
            stock.getVolatility() != null ? stock.getVolatility() : 0,
            stock.getBollingerUpper() != null ? stock.getBollingerUpper() : 0,
            stock.getBollingerLower() != null ? stock.getBollingerLower() : 0,
            stock.getMacd() != null ? stock.getMacd() : 0,
            stock.getMacdSignal() != null ? stock.getMacdSignal() : 0,
            stock.getStatusMessage() != null ? stock.getStatusMessage() : "");
        contentStream.showText(line);
        contentStream.endText();
    }
}
