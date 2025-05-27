# stock-market-analyzer

A robust, multithreaded Spring Boot application for real-time and historical stock market data aggregation, analytics, and strategy simulation. Designed for flexibility, performance, and extensibility, this project demonstrates concurrent data fetching, analytics computation, and export capabilities—all with a modern REST API and interactive documentation.

## Key Features
- **Concurrent Data Fetching:** Efficiently fetches real-time and historical stock data for multiple symbols in parallel using a thread pool (ExecutorService).
- **Customizable Analytics:** Calculates moving averages, volatility, EMA, RSI and simulates a simple moving average trading strategy. All analytics periods are user-configurable per request.
- **Historical Data Range Selection:** Analyze stock data for a custom date range using `startDate` and `endDate` in your request.
- **Robust Error Handling:** Gracefully manages API rate limits, network errors, and invalid symbols.
- **Flexible Output:** Results can be returned as JSON, or exported as CSV, Excel (XLSX), or PDF files for further analysis or reporting.
- **Interactive API Documentation:** Integrated Swagger UI (OpenAPI) for easy exploration and testing of all endpoints.
- **Modern Java & Spring Boot:** Built with Java 17 and Spring Boot 3.x for maximum compatibility and maintainability.

## Getting Started

- **Build:** `./mvnw clean package`
- **Run:** `./mvnw spring-boot:run`
- **Java Version:** 17
- **Spring Boot:** 3.x

## API Endpoints

### Analyze Stocks (JSON)
```
POST /api/stocks/analyze
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20,
  "startDate": "2024-01-01",
  "endDate": "2024-05-01"
}
```
Returns analytics and simulation results for each symbol as JSON. If `startDate` and `endDate` are provided, analytics are computed only for that date range.

### Download CSV
```
POST /api/stocks/analyze/csv
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20,
  "startDate": "2024-01-01",
  "endDate": "2024-05-01"
}
```
Returns a CSV file with analytics and simulation results for the specified date range.

**CSV Columns:**
- Symbol
- Latest Price
- Moving Average
- EMA
- RSI
- Volatility
- Bollinger Upper
- Bollinger Lower
- MACD
- MACD Signal
- Status Message

### Download Excel
```
POST /api/stocks/analyze/excel
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20,
  "startDate": "2024-01-01",
  "endDate": "2024-05-01"
}
```
Returns an Excel (.xlsx) file with analytics and simulation results for the specified date range.

**Excel Columns:**
- Symbol
- Latest Price
- Moving Average
- EMA
- RSI
- Volatility
- Bollinger Upper
- Bollinger Lower
- MACD
- MACD Signal
- Status Message

### Download PDF
```
POST /api/stocks/analyze/pdf
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20,
  "startDate": "2024-01-01",
  "endDate": "2024-05-01"
}
```
Returns a PDF file with analytics and simulation results for the specified date range.

**PDF Columns:**
- Symbol
- Latest Price
- Moving Average
- EMA
- RSI
- Volatility
- Bollinger Upper
- Bollinger Lower
- MACD
- MACD Signal
- Status Message

## Interactive API Documentation

Once the app is running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html
- or http://localhost:8080/swagger-ui/index.html

Explore, test, and understand all endpoints and request/response formats interactively.

## Configuration
Add your Alpha Vantage API key to `src/main/resources/application.properties`:
```
alphavantage.api.key=YOUR_ALPHA_VANTAGE_API_KEY
```

## Example Usage
```
curl -X POST http://localhost:8080/api/stocks/analyze -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20,"startDate":"2024-01-01","endDate":"2024-05-01"}'
curl -X POST http://localhost:8080/api/stocks/analyze/csv -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20,"startDate":"2024-01-01","endDate":"2024-05-01"}' -o stock-analysis.csv
curl -X POST http://localhost:8080/api/stocks/analyze/excel -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20,"startDate":"2024-01-01","endDate":"2024-05-01"}' -o stock-analysis.xlsx
curl -X POST http://localhost:8080/api/stocks/analyze/pdf -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20,"startDate":"2024-01-01","endDate":"2024-05-01"}' -o stock-analysis.pdf
```

---