# Banking Platform

A lightweight, conventional Java Spring Boot banking platform that serves as a baseline application for future capabilities.

## Architecture

The application uses a conventional layered architecture with feature-oriented organization:

```
com.example.bankingplatform/
├── customer/        - Customer domain
├── account/         - Account domain
├── transaction/     - Transaction domain
├── support/         - Support ticket domain
└── common/          - Shared components (exception handling, config)
```

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Flyway** for database migrations
- **JUnit 5** + **Mockito** for testing
- **OpenAPI/Swagger** for API documentation
- **Maven** for build management

## How to Run Locally

### Prerequisites

- Java 21 or higher
- PostgreSQL database running
- Maven 3.8+

### Configuration

The application uses a `.env` file for database credentials.

1. Copy the example file:
```bash
cp .env.example .env
```

2. Update `.env` with your PostgreSQL credentials:
```
DB_URL=jdbc:postgresql://localhost:5432/banking_platform
DB_USERNAME=your_username
DB_PASSWORD=your_password
SERVER_PORT=8080
```

The `.env` file is gitignored and won't be committed to the repository.

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### Access Swagger UI

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## How to Run Tests

```bash
mvn test
```

Or to skip tests during build:
```bash
mvn clean install -DskipTests
```

## API Documentation

The API is documented using OpenAPI 3.0. Access the interactive documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Available Endpoints

#### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer
- `GET /api/customers/{id}/accounts` - Get customer accounts
- `GET /api/customers/{id}/transactions` - Get customer transactions
- `GET /api/customers/{id}/support-tickets` - Get customer support tickets
- `GET /api/customers/{id}/context` - Get customer context (for AI/RAG)

#### Accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create new account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account
- `GET /api/accounts/{id}/transactions` - Get account transactions

#### Transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `POST /api/transactions` - Create new transaction
- `GET /api/transactions` - Get transactions with filters
- `GET /api/transactions/{id}/context` - Get transaction context (for fraud detection)

#### Support Tickets
- `GET /api/support/tickets/{id}` - Get support ticket by ID
- `POST /api/support/tickets` - Create new support ticket
- `PUT /api/support/tickets/{id}` - Update support ticket
- `GET /api/support/tickets/customer/{customerId}` - Get customer support tickets

## Database Schema

### Tables

#### customers
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| phone | VARCHAR(50) | |
| date_of_birth | DATE | |
| country | VARCHAR(10) | NOT NULL |
| status | ENUM | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

#### accounts
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| customer_id | BIGINT | NOT NULL, FK |
| account_number | VARCHAR(50) | NOT NULL, UNIQUE |
| account_type | ENUM | NOT NULL |
| currency | VARCHAR(3) | NOT NULL |
| balance | DECIMAL(15,2) | NOT NULL |
| status | ENUM | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

#### transactions
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| account_id | BIGINT | NOT NULL, FK |
| transaction_type | ENUM | NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| currency | VARCHAR(3) | NOT NULL |
| merchant_name | VARCHAR(255) | |
| merchant_category | ENUM | |
| country | VARCHAR(10) | |
| city | VARCHAR(100) | |
| transaction_timestamp | TIMESTAMP | NOT NULL |
| status | ENUM | NOT NULL |
| description | TEXT | |
| created_at | TIMESTAMP | NOT NULL |

#### support_tickets
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| customer_id | BIGINT | NOT NULL, FK |
| subject | VARCHAR(255) | NOT NULL |
| description | TEXT | NOT NULL |
| category | ENUM | NOT NULL |
| priority | ENUM | NOT NULL |
| status | ENUM | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| resolved_at | TIMESTAMP | |

### Indexes

- `idx_customers_email` on customers(email)
- `idx_customers_status` on customers(status)
- `idx_accounts_customer_id` on accounts(customer_id)
- `idx_accounts_status` on accounts(status)
- `idx_transactions_account_id` on transactions(account_id)
- `idx_transactions_timestamp` on transactions(transaction_timestamp)
- `idx_transactions_status` on transactions(status)
- `idx_support_tickets_customer_id` on support_tickets(customer_id)
- `idx_support_tickets_status` on support_tickets(status)

## Example API Requests

### Create a Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com",
    "phone": "+1-555-0100",
    "dateOfBirth": "1990-01-15",
    "country": "US",
    "status": "ACTIVE"
  }'
```

### Create an Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {
      "id": 1
    },
    "accountNumber": "ACC-001",
    "accountType": "CHECKING",
    "currency": "USD",
    "balance": 5000.00,
    "status": "ACTIVE"
  }'
```

### Create a Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "account": {
      "id": 1
    },
    "transactionType": "PURCHASE",
    "amount": 150.50,
    "currency": "USD",
    "merchantName": "Test Store",
    "merchantCategory": "GROCERY",
    "country": "US",
    "city": "New York",
    "status": "COMPLETED"
  }'
```

### Get Customer Context

```bash
curl http://localhost:8080/api/customers/1/context
```

### Get Transaction Context

```bash
curl http://localhost:8080/api/transactions/1/context
```

### Filter Transactions

```bash
curl "http://localhost:8080/api/transactions?accountId=1&status=COMPLETED&from=2026-01-01T00:00:00&to=2026-12-31T23:59:59"
```

## Error Handling

The API uses standard HTTP status codes:

- `400 Bad Request` - Validation errors
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate email or account number
- `500 Internal Server Error` - Server error

Error response format:
```json
{
  "timestamp": "2026-09-01T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 123",
  "path": "/api/customers/123"
}
```

## Actuator Endpoints

- Health check: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`

## Demo Data

The application comes with demo data including:
- 20 customers
- 30 accounts
- 500+ transactions
- 30+ support tickets

## Development

### Build

```bash
mvn clean install
```

### Run with specific profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## License

This is a demonstration project for educational purposes.