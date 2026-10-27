# Banking Platform

A lightweight, conventional Java Spring Boot banking platform with an AI-powered customer support assistant.

## Architecture

The application uses a conventional layered architecture with feature-oriented organization:

```
com.example.bankingplatform/
├── customer/        - Customer domain
├── account/         - Account domain
├── transaction/     - Transaction domain
├── support/         - Support ticket domain
├── ai/              - AI-powered support (LangGraph4j, RAG)
└── common/          - Shared components (exception handling, config)
```

### AI Module

The `ai/` module provides an AI customer support assistant with:

- **LangGraph4j workflows** — multi-turn stateful conversations for transaction history, support tickets, and lost/stolen card reporting (uses `AgentState` with typed accessors)
- **RAG (Retrieval-Augmented Generation)** — knowledge base Q&A using Qdrant vector store (uses `ChatLanguageModel` for text generation)
- **Tool-based architecture** — AI interacts with banking data through explicitly defined tools (FindCustomer, GetRecentTransactions, GetSupportTickets, CreateSupportTicket, SearchDocument)
- **Case-insensitive confirmation** — `CONFIRM`/`CANCEL` accepted in any case (e.g., "confirm", "Confirm", "CONFIRM")
- **Graph nodes** — `ResolveIdentityNode`, `GetTransactionsNode`, `GetSupportTicketsNode`, `ConfirmNode`, `ConfirmPromptNode`, `CreateTicketNode`, `RespondNode`
- **Chat widget** — unified `/v1/api/chat` endpoint for the chat UI with auto-routing between RAG and LangGraph workflows

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Flyway** for database migrations
- **LangChain4j** — LLM integration, tools, embeddings
- **LangGraph4j** — stateful graphs, nodes, edges, checkpoints
- **Qdrant** — vector store for RAG (384-dim, cosine similarity)
- **MiniLM** — in-process embedding model (ONNX, 384-dim)
- **JUnit 5** + **Mockito** for testing
- **OpenAPI/Swagger** for API documentation
- **Maven** for build management

## How to Run Locally

### Prerequisites

- Java 21 or higher
- PostgreSQL database running
- Maven 3.8+
- **For AI features:**
  - LLM server (vLLM or llama.cpp) on port 8000
  - Qdrant vector store on port 6333

### Configuration

The application uses a `.env` file for all credentials and configuration.

1. Copy the example file:
```bash
cp .env.example .env
```

2. Update `.env` with your credentials:

**Database:**
```
DB_URL=jdbc:postgresql://localhost:5432/banking_platform
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

**LLM Server (for AI features):**
```
LLM_URL=http://localhost:8000/v1
LLM_MODEL=openai/gpt-oss-20b
LLM_API_KEY=your-api-key
LLM_TEMPERATURE=0.2
LLM_MAX_TOKENS=1024
LLM_TIMEOUT=30s
```

**Qdrant (for AI features):**
```
QDRANT_URL=http://localhost:6333
QDRANT_COLLECTION=knowledge_documents
```

**Knowledge Base:**
```
KNOWLEDGE_BASE_DIR=classpath:knowledge-base
```

**AI Agent:**
```
AI_MAX_TOOL_CALLS=5
AI_SESSION_TIMEOUT=30m
```

**RAG Pipeline:**
```
AI_RAG_RETRIEVAL_TOP_K=5
AI_RAG_RETRIEVAL_SIMILARITY_THRESHOLD=0.75
AI_RAG_MAX_CONTEXT_TOKENS=3000
AI_RAG_CHUNK_SIZE=800
AI_RAG_CHUNK_OVERLAP=150
AI_RAG_CHUNK_MAX_ITERATIONS=1000
```

**Server:**
```
SERVER_PORT=8080
```

**JPA:**
```
JPA_DDL_AUTO=update
SHOW_SQL=false
```

**Logging:**
```
LOGGING_LEVEL=INFO
LOGGING_LEVEL_APACHE=DEBUG
LOGGING_LEVEL_HIBERNATE_POOL=WARN
```

**Actuator:**
```
ACTUATOR_ENDPOINTS=health,info
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

#### AI Support Chat (LangGraph)
- `POST /api/ai/support/chat` - Send a message to the AI support agent
  - Request: `{sessionId, workflow, message, confirmationAction?}`
  - Workflows: `TRANSACTION_HISTORY`, `LOST_OR_STOLEN_CARD`
  - States: `AWAITING_CUSTOMER_IDENTITY`, `AWAITING_CONFIRMATION`, `COMPLETED`

#### AI Chat Widget
- `GET /api/ai/chat/widget` - Chat widget endpoint

#### AI RAG (Knowledge Base Q&A)
- `POST /api/ai/support/rag` - Ask a general knowledge question
  - Request: `{sessionId, message}`
  - Answers from the knowledge base with source attribution

#### Knowledge Base Management
- `GET /v1/api/kb` - List knowledge base documents (optional `?category=` filter)
- `GET /v1/api/kb/{id}` - Get document by ID (with chunks)
- `DELETE /v1/api/kb/{id}` - Delete document, its chunks, and Qdrant embeddings
- `POST /v1/api/kb/reindex` - Reindex knowledge base (skip unchanged)
- `POST /v1/api/kb/reindex?force=true` - Full clear + reingest

## AI Features

### Workflows

The AI support agent handles three workflows:

1. **Transaction History** — Customer asks about recent transactions. The agent resolves identity, fetches the 3 most recent transactions, and presents them in natural language.

2. **Lost/Stolen Card** — Customer reports a lost or stolen card. The agent resolves identity, requests explicit CONFIRM/CANCEL confirmation, and creates a high-priority security support ticket.

3. **Support Tickets** — Customer asks about their support tickets. The agent resolves identity and retrieves all open support tickets.

4. **General Knowledge (RAG)** — Customer asks general questions (e.g., branch hours, transaction processing times). The system retrieves relevant knowledge base documents and generates a grounded response with source attribution.

### Knowledge Base

The RAG system uses markdown documents stored in the knowledge base directory:

- Documents are parsed for title, category, and checksum
- Content is chunked (~800 chars, 150 overlap)
- Chunks are embedded via MiniLM and stored in Qdrant
- Retrieval uses cosine similarity with configurable thresholds

**Categories:** `ACCOUNT_POLICY`, `PAYMENT_POLICY`, `SECURITY_POLICY`, `TRANSACTION_POLICY`, `FAQ`, `PRODUCT_DOCUMENTATION`, `GENERAL`

### AI API Example

**Send a message to the support agent:**
```bash
curl -X POST http://localhost:8080/api/ai/support/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "workflow": "TRANSACTION_HISTORY",
    "message": "John Smith"
  }'
```

**Ask a general knowledge question:**
```bash
curl -X POST http://localhost:8080/api/ai/support/rag \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "message": "What hours are the branches open?"
  }'
```

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