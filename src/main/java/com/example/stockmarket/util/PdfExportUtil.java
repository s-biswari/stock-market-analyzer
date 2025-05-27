package com.example.stockmarket.util;

import com.example.stockmarket.model.StockData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import jakarta.servlet.ServletOutputStream;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;

public class PdfExportUtil {
    private PdfExportUtil() {
        // Prevent instantiation
    }

    public static void writeStockDataToPdf(Map<String, StockData> data, ServletOutputStream out) throws IOException {
        final float margin = 40;
        final float tableTopY = PDRectangle.LETTER.getHeight() - margin;
        final float rowHeight = 18;
        // Adjusted column widths to fit within 612 - 2*40 = 532pt
        final float[] colWidths = {45, 50, 35, 35, 45, 45, 50, 50, 35, 45, 87};
        final String[] headers = {"Symbol", "Latest Price", "EMA", "RSI", "Moving Avg", "Volatility", "Bollinger Upper", "Bollinger Lower", "MACD", "MACD Signal", "Status"};
        final int fontSize = 8;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float y = tableTopY;
            // Draw header row
            y = drawTableRow(contentStream, headers, y, colWidths, true, fontSize);
            // Draw data rows
            for (StockData stock : data.values()) {
                String[] row = {
                    stock.getSymbol(),
                    formatDouble(stock.getLatestPrice()),
                    formatDouble(stock.getEma()),
                    formatDouble(stock.getRsi()),
                    formatDouble(stock.getMovingAverage()),
                    formatDouble(stock.getVolatility()),
                    formatDouble(stock.getBollingerUpper()),
                    formatDouble(stock.getBollingerLower()),
                    formatDouble(stock.getMacd()),
                    formatDouble(stock.getMacdSignal()),
                    stock.getStatusMessage() != null ? stock.getStatusMessage() : ""
                };
                float neededHeight = getRowHeightForWrappedText(row, colWidths, contentStream, rowHeight, fontSize);
                if (y - neededHeight < margin) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    y = tableTopY;
                    y = drawTableRow(contentStream, headers, y, colWidths, true, fontSize);
                }
                y = drawTableRow(contentStream, row, y, colWidths, false, fontSize);
            }
            contentStream.close();
            document.save(out);
        }
    }

    private static float drawTableRow(PDPageContentStream contentStream, String[] cells, float y, float[] colWidths, boolean isHeader, int fontSize) throws IOException {
        float x = 40;
        float maxHeight = 18;
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        contentStream.setFont(isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, fontSize);
        // Calculate max height for wrapped text
        for (int i = 0; i < cells.length; i++) {
            float cellHeight = getWrappedTextHeight(cells[i], colWidths[i], fontSize);
            if (cellHeight > maxHeight) maxHeight = cellHeight;
        }
        // Draw cell rectangles
        for (int i = 0; i < cells.length; i++) {
            contentStream.addRect(x, y - maxHeight, colWidths[i], maxHeight);
            contentStream.stroke();
            x += colWidths[i];
        }
        // Draw text
        x = 40;
        for (int i = 0; i < cells.length; i++) {
            drawWrappedText(contentStream, cells[i], x + 2, y - 5, colWidths[i] - 4, fontSize);
            x += colWidths[i];
        }
        return y - maxHeight;
    }

    private static void drawWrappedText(PDPageContentStream contentStream, String text, float x, float y, float width, int fontSize) throws IOException {
        PDType1Font font = PDType1Font.HELVETICA;
        float leading = 1.2f * fontSize;
        String[] lines = wrapText(text, font, fontSize, width);
        for (String line : lines) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(line);
            contentStream.endText();
            y -= leading;
        }
    }

    private static String[] wrapText(String text, PDType1Font font, int fontSize, float width) throws IOException {
        if (text == null) return new String[]{""};
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float size = font.getStringWidth(testLine) / 1000 * fontSize;
            if (size > width && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines.toArray(new String[0]);
    }

    private static float getWrappedTextHeight(String text, float width, int fontSize) throws IOException {
        PDType1Font font = PDType1Font.HELVETICA;
        String[] lines = wrapText(text, font, fontSize, width);
        return lines.length * 1.2f * fontSize;
    }

    private static float getRowHeightForWrappedText(String[] row, float[] colWidths, PDPageContentStream contentStream, float minHeight, int fontSize) throws IOException {
        float max = minHeight;
        for (int i = 0; i < row.length; i++) {
            float h = getWrappedTextHeight(row[i], colWidths[i], fontSize);
            if (h > max) max = h;
        }
        return max;
    }

    private static String formatDouble(Double d) {
        return d != null ? String.format("%.2f", d) : "";
    }
}
