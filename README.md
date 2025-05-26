# stock-market-analyzer

This is a Spring Boot project for a Multithreaded Stock Market Data Aggregator & Analyzer.

## Getting Started

- **Build:** `./mvnw clean package`
- **Run:** `./mvnw spring-boot:run`
- **Java Version:** 17
- **Spring Boot:** 3.x

## Features
- Fetches real-time and historical stock data from the Alpha Vantage API for multiple symbols concurrently
- Uses a thread pool (ExecutorService) for parallel data fetching
- Calculates analytics: moving averages, volatility, and more
- Simulates a simple moving average trading strategy
- Handles API rate limits and errors robustly
- Exports results as JSON, CSV, or Excel (XLSX)

## API Endpoints

### Analyze Stocks (JSON)
```
POST /api/stocks/analyze
Content-Type: application/json
Body: ["AAPL", "GOOGL", "MSFT"]
```
Returns analytics and simulation results for each symbol as JSON.

### Download CSV
```
POST /api/stocks/analyze/csv
Content-Type: application/json
Body: ["AAPL", "GOOGL", "MSFT"]
```
Returns a CSV file with analytics and simulation results.

### Download Excel
```
POST /api/stocks/analyze/excel
Content-Type: application/json
Body: ["AAPL", "GOOGL", "MSFT"]
```
Returns an Excel (.xlsx) file with analytics and simulation results.

## Configuration
Add your Alpha Vantage API key to `src/main/resources/application.properties`:
```
alphavantage.api.key=YOUR_ALPHA_VANTAGE_API_KEY
```

## Example Usage
```
curl -X POST http://localhost:8080/api/stocks/analyze -H "Content-Type: application/json" -d '["AAPL","GOOGL","MSFT"]'
curl -X POST http://localhost:8080/api/stocks/analyze/csv -H "Content-Type: application/json" -d '["AAPL","GOOGL","MSFT"]' -o stock-analysis.csv
curl -X POST http://localhost:8080/api/stocks/analyze/excel -H "Content-Type: application/json" -d '["AAPL","GOOGL","MSFT"]' -o stock-analysis.xlsx
```

---
