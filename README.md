# 💳 Modular Banking System

A production-ready backend banking system built using Spring Boot, Kafka, Docker, and Protobuf — designed for modularity and extensibility.

---

## ✅ Features Implemented

- **Account Service**: 
  - Create, get, deposit, withdraw, delete accounts
  - Soft delete, optimistic locking (@Version), pagination, audit fields, input validation
  - Emits Kafka events for transactions using Protobuf

- **Transaction Service**:
  - Consumes Kafka events
  - Logs deposit and withdrawal transactions

- **Integration Tests**:
  - Cross-service integration tests via RestAssured
  - Covers concurrency, event flow, and edge cases

---

## 🐳 Run with Docker

```bash
docker-compose up --build -d
```

---

## 🧪 Run Integration Tests

```bash
cd integration-tests
./mvnw test
```

Tests will validate:

- Account creation
- Deposit/Withdraw behavior
- Kafka event production/consumption
- Concurrency handling

---

## 🔍 Test Endpoints (Postman / curl)

### ➕ Create Account

```bash
POST http://localhost:8081/api/accounts
Content-Type: application/json

{
  "accountHolder": "John Doe",
  "balance": 1000.0,
  "customerId": 1,
  "type": "SAVINGS"
}
```

### 💰 Deposit

```bash
POST http://localhost:8081/api/accounts/deposit
{
  "accountId": 1,
  "amount": 500.0
}
```

### 💸 Withdraw

```bash
POST http://localhost:8081/api/accounts/withdraw
{
  "accountId": 1,
  "amount": 200.0
}
```

### 📜 Get Transactions

```bash
GET http://localhost:8082/api/transactions/account/1
```

---
