# stock-market-analyzer

A robust, multithreaded Spring Boot application for real-time and historical stock market data aggregation, analytics, and strategy simulation. Designed for flexibility, performance, and extensibility, this project demonstrates concurrent data fetching, analytics computation, and export capabilities—all with a modern REST API and interactive documentation.

## Key Features
- **Concurrent Data Fetching:** Efficiently fetches real-time and historical stock data for multiple symbols in parallel using a thread pool (ExecutorService).
- **Customizable Analytics:** Calculates moving averages, volatility, and simulates a simple moving average trading strategy. All analytics periods are user-configurable per request.
- **Robust Error Handling:** Gracefully manages API rate limits, network errors, and invalid symbols.
- **Flexible Output:** Results can be returned as JSON, or exported as CSV or Excel (XLSX) files for further analysis or reporting.
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
  "longMAPeriod": 20
}
```
Returns analytics and simulation results for each symbol as JSON.

### Download CSV
```
POST /api/stocks/analyze/csv
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20
}
```
Returns a CSV file with analytics and simulation results.

### Download Excel
```
POST /api/stocks/analyze/excel
Content-Type: application/json
Body: {
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "movingAveragePeriod": 10,
  "volatilityPeriod": 10,
  "shortMAPeriod": 5,
  "longMAPeriod": 20
}
```
Returns an Excel (.xlsx) file with analytics and simulation results.

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
curl -X POST http://localhost:8080/api/stocks/analyze -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20}'
curl -X POST http://localhost:8080/api/stocks/analyze/csv -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20}' -o stock-analysis.csv
curl -X POST http://localhost:8080/api/stocks/analyze/excel -H "Content-Type: application/json" -d '{"symbols":["AAPL","GOOGL","MSFT"],"movingAveragePeriod":10,"volatilityPeriod":10,"shortMAPeriod":5,"longMAPeriod":20}' -o stock-analysis.xlsx
```

---