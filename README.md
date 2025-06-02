# stock-market-analyzer

A robust, multithreaded Spring Boot application for real-time and historical stock market data aggregation, analytics, and strategy simulation. Now includes portfolio management, analytics, and PDF/Excel/CSV export features with PostgreSQL persistence.

## Key Features
- **Concurrent Data Fetching:** Efficiently fetches real-time and historical stock data for multiple symbols in parallel using a thread pool (ExecutorService).
- **Customizable Analytics:** Calculates moving averages, volatility, EMA, RSI and simulates a simple moving average trading strategy. All analytics periods are user-configurable per request.
- **Portfolio Management:** Create, update, and delete portfolios and manage stocks within each portfolio.
- **Portfolio Analytics:** Calculate and retrieve portfolio-level analytics (total value, cost, P&L, allocation breakdown) via a dedicated API endpoint.
- **Historical Data Range Selection:** Analyze stock data for a custom date range using `startDate` and `endDate` in your request.
- **Robust Error Handling:** Gracefully manages API rate limits, network errors, and invalid symbols.
- **Flexible Output:** Results can be returned as JSON, or exported as CSV, Excel (XLSX), or PDF files for further analysis or reporting.
- **Interactive API Documentation:** Integrated Swagger UI (OpenAPI) for easy exploration and testing of all endpoints.
- **Modern Java & Spring Boot:** Built with Java 17 and Spring Boot 3.x for maximum compatibility and maintainability.
- **PostgreSQL Persistence:** All portfolios and stocks are persisted using Spring Data JPA and PostgreSQL.

## Getting Started

- **Build:** `./mvnw clean package`
- **Run:** `./mvnw spring-boot:run`
- **Java Version:** 17
- **Spring Boot:** 3.x
- **Database:** PostgreSQL (see configuration below)

## Portfolio Management & Analytics API

### Create Portfolio
```
POST /api/portfolios
Content-Type: application/json
Body: { "name": "My Portfolio", "owner": "username" }
```

### Add Stock to Portfolio
```
POST /api/portfolios/{portfolioId}/stocks
Content-Type: application/json
Body: { "symbol": "AAPL", "quantity": 10, "buyPrice": 150.0 }
```

### Get Portfolio Details
```
GET /api/portfolios/{id}
```

### Get All Portfolios (or by owner)
```
GET /api/portfolios
GET /api/portfolios?owner=username
```

### Portfolio Analytics (P&L, Allocation, etc.)
```
POST /api/portfolios/{portfolioId}/analytics
```
Returns:
```
{
  "portfolioId": 1,
  "name": "My Portfolio",
  "owner": "username",
  "totalValue": 12345.67,
  "totalCost": 12000.00,
  "pnl": 345.67,
  "allocations": [
    {
      "symbol": "AAPL",
      "allocation": 60.0,
      "positionValue": 7400.00,
      "buyPrice": 150.0,
      "currentPrice": 170.0,
      "quantity": 50
    },
    ...
  ]
}
```

## Stock Analytics API (Legacy)

### Analyze Stocks (JSON)
```
POST /api/stocks/analyze
Content-Type: application/json
Body: { ... }
```

### Download CSV/Excel/PDF
```
POST /api/stocks/analyze/csv|excel|pdf
Content-Type: application/json
Body: { ... }
```

## Database Configuration
Add your PostgreSQL settings to `src/main/resources/application.properties`:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/sma_db
spring.datasource.username=sma_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
```

## Redis Integration

### Caching Stock Prices
- Redis is used to cache stock prices for improved performance.
- Cached data expires after 12 hours.

### Redis Configuration
Add Redis settings to `src/main/resources/application.properties`:
```
spring.redis.host=localhost
spring.redis.port=6379
```

### Inspect Redis Keys
Use the following commands to inspect Redis keys:
```
redis-cli
KEYS *
GET <key>
TTL <key>
```

### Clear Redis Keys
To clear all keys in Redis:
```
FLUSHALL
```

---

## Interactive API Documentation
Once the app is running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html
- or http://localhost:8080/swagger-ui/index.html

## Example Usage
```
curl -X POST http://localhost:8080/api/portfolios -H "Content-Type: application/json" -d '{"name":"My Portfolio","owner":"sbiswari"}'
curl -X POST http://localhost:8080/api/portfolios/1/stocks -H "Content-Type: application/json" -d '{"symbol":"AAPL","quantity":10,"buyPrice":150.0}'
curl -X POST http://localhost:8080/api/portfolios/1/analytics
```

---

For more, see the Swagger UI or source code.