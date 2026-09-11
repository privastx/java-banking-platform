 # Spring Boot - Banking Platform

 # 1\. Purpose

 Build a lightweight, conventional Java Spring Boot banking platform that will serve as the baseline application to which future capabilities can be added to with minimal changes.

---

 # 2\. Technology Requirements

 Use the following technologies unless there is a compelling technical reason otherwise.

 ### Backend

 - Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven

 ### Database

 - PostgreSQL
- Flyway for database migrations

 ### Testing

 - JUnit 5
- Mockito
- Spring Boot Test

 ### API Documentation

 - OpenAPI / Swagger

 ### Build
```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

### Testing

```bash
mvn test
```


 # 3\. Architectural Goals

 The application should use a conventional layered architecture.

 Recommended structure:

```
src/main/java/com/example/bankingplatform/

    BankingPlatformApplication.java

    customer/
        Customer.java
        CustomerRepository.java
        CustomerService.java
        CustomerController.java
        CustomerDto.java

    account/
        Account.java
        AccountRepository.java
        AccountService.java
        AccountController.java
        AccountDto.java

    transaction/
        Transaction.java
        TransactionRepository.java
        TransactionService.java
        TransactionController.java
        TransactionDto.java

    support/
        SupportTicket.java
        SupportTicketRepository.java
        SupportTicketService.java
        SupportTicketController.java
        SupportTicketDto.java

    common/
        exception/
        validation/
        config/
```

 Feature-oriented organization is preferred over one large collection of controllers/services/repositories.

 Do not introduce unnecessary microservices.

 The entire base application should initially be **one Spring Boot application**.

---

 # 4\. Domain Model

 The application should contain four primary business areas:

```
Customer
   │
   ├───────────────┐
   │               │
   ▼               ▼
Account       SupportTicket
   │
   ▼
Transaction
```

 ## 4.1 Customer

 Represents a banking customer.

 Fields:

```
id
firstName
lastName
email
phone
dateOfBirth
country
status
createdAt
updatedAt
```

 Customer status:

```
ACTIVE
SUSPENDED
CLOSED
```

 Requirements:

 - Email must be unique.
- Customer must have a creation timestamp.
- Customer must have a status.
- Customer should have relationships to accounts and support tickets.
- Do not store unnecessary personally identifiable information.

---

 # 5\. Account

 Represents a customer's bank account.

 Fields:

```
id
customerId
accountNumber
accountType
currency
balance
status
createdAt
updatedAt
```

 Account types:

```
CHECKING
SAVINGS
```

 Account statuses:

```
ACTIVE
FROZEN
CLOSED
```

 Requirements:

 - An account belongs to exactly one customer.
- Account number must be unique.
- Currency should initially support USD.
- Balance should use `BigDecimal`.
- Monetary values must never use floating-point types.
- Account status must be represented as an enum.

---

 # 6\. Transaction

 A transaction represents a bank transaction associated with an account.

 Fields:

```
id
accountId
transactionType
amount
currency
merchantName
merchantCategory
country
city
transactionTimestamp
status
description
createdAt
```

 Transaction types:

```
PURCHASE
WITHDRAWAL
DEPOSIT
TRANSFER
PAYMENT
REFUND
```

 Transaction statuses:

```
PENDING
COMPLETED
FAILED
REVERSED
```

 Merchant categories should include examples such as:

```
GROCERY
RESTAURANT
TRAVEL
ELECTRONICS
ENTERTAINMENT
HEALTHCARE
GAS_STATION
ONLINE_RETAIL
OTHER
```

 Requirements:

 - `amount` must use `BigDecimal`.
- Transactions must have timestamps.
- Transactions must have a status.
- Transactions must be associated with an account.
- Merchant information should be nullable for transaction types where it is not applicable.
- Country should be represented as an ISO country code.
- The model should provide enough structured information to support future fraud-detection feature engineering.

 Do not implement fraud detection in this phase.

---

 # 7\. Support Ticket

 Support tickets provide the foundation for the future LLM/RAG customer-support capability.

 Fields:

```
id
customerId
subject
description
category
priority
status
createdAt
updatedAt
resolvedAt
```

 Categories:

```
ACCOUNT
TRANSACTION
PAYMENT
CARD
SECURITY
GENERAL
```

 Priority:

```
LOW
MEDIUM
HIGH
URGENT
```

 Status:

```
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

 Requirements:

 - A ticket belongs to a customer.
- Tickets should retain their original description.
- Tickets should have timestamps.
- Tickets should be searchable by customer.
- The application should support retrieving a customer's ticket history.

 The ticket data will eventually become useful context for the customer-support AI.

---

 # 8\. Relationships

 The expected relationships are:

```
Customer
   │
   ├── 1:N ── Account
   │
   └── 1:N ── SupportTicket

Account
   │
   └── 1:N ── Transaction
```

 Use appropriate JPA relationships.

 Avoid exposing raw JPA entities directly from the REST API where doing so would create serialization or coupling problems. Prefer DTOs.

---

 # 9\. REST API

 Implement a simple REST API.

 ## Customers

```
GET    /api/customers
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

 Additional endpoint:

```
GET /api/customers/{id}/accounts
GET /api/customers/{id}/transactions
GET /api/customers/{id}/support-tickets
```

---

 ## Accounts

```
GET    /api/accounts/{id}
POST   /api/accounts
PUT    /api/accounts/{id}
DELETE /api/accounts/{id}
```

 Additional endpoint:

```
GET /api/accounts/{id}/transactions
```

---

 ## Transactions

```
GET  /api/transactions/{id}
POST /api/transactions
```

 Support basic filtering:

```
GET /api/transactions?accountId=123
GET /api/transactions?customerId=123
GET /api/transactions?status=COMPLETED
GET /api/transactions?from=2026-01-01&to=2026-01-31
```

 The filtering implementation can be simple. Do not build an elaborate search engine.

---

 ## Support Tickets

```
GET  /api/support/tickets/{id}
POST /api/support/tickets
PUT  /api/support/tickets/{id}
```

 Support:

```
GET /api/customers/{id}/support-tickets
```

---

 # 10\. Customer Context Endpoint

 Create one particularly important endpoint:

```
GET /api/customers/{id}/context
```

 This endpoint should return a consolidated view of information about a customer.

 Example:

```
{
  "customer": {
    "id": 123,
    "firstName": "Jane",
    "lastName": "Doe",
    "status": "ACTIVE"
  },
  "accounts": [
    {
      "id": 1001,
      "accountType": "CHECKING",
      "currency": "USD",
      "balance": 5234.21,
      "status": "ACTIVE"
    }
  ],
  "recentTransactions": [
    {
      "id": 9001,
      "amount": 142.32,
      "merchantName": "Example Store",
      "merchantCategory": "GROCERY",
      "status": "COMPLETED"
    }
  ],
  "supportTickets": [
    {
      "id": 5001,
      "subject": "Question about transaction",
      "status": "OPEN"
    }
  ]
}
```

 This endpoint is intentionally useful for a future RAG/customer-support service.

 The future AI service should be able to call this endpoint without requiring direct database access.

---

 # 11\. Transaction Context Endpoint

 Create:

```
GET /api/transactions/{id}/context
```

 It should provide enough information for a future fraud-detection service.

 Example:

```
{
  "transaction": {
    "id": 9001,
    "amount": 1842.17,
    "currency": "USD",
    "merchantName": "Example Electronics",
    "merchantCategory": "ELECTRONICS",
    "country": "US",
    "city": "Austin",
    "transactionTimestamp": "2026-09-01T02:14:32Z",
    "status": "COMPLETED"
  },
  "customer": {
    "id": 123,
    "country": "US"
  },
  "account": {
    "id": 1001,
    "accountType": "CHECKING"
  }
}
```

 The endpoint should not calculate a fraud score.

---

 # 12\. Database

 Use PostgreSQL.

 Expected tables:

```
customers
accounts
transactions
support_tickets
```

 Use Flyway migrations.

 Example:

```
V1__create_customers.sql
V2__create_accounts.sql
V3__create_transactions.sql
V4__create_support_tickets.sql
V5__insert_demo_data.sql
```

 Database constraints should include:

 - Primary keys
- Foreign keys
- Unique customer email
- Unique account number
- Appropriate NOT NULL constraints
- Appropriate indexes

 Add indexes on fields likely to be queried frequently, especially:

```
transactions.account_id
transactions.transaction_timestamp
transactions.status
support_tickets.customer_id
support_tickets.status
```

---

 # 13\. Demo Data

 Provide realistic seed data.

 The application should start with enough data to make the system interesting.

 Minimum:

```
20 customers
30 accounts
500+ transactions
30+ support tickets
```

 The transactions should contain meaningful variation.

 For example:

 - Different transaction amounts
- Different merchants
- Different merchant categories
- Different countries
- Different cities
- Different timestamps
- Different transaction types
- Different statuses

 Include a small number of intentionally unusual transaction patterns.

 However:

 **Do not label transactions as fraudulent in the production domain model.**

 If labeled data is required later for training XGBoost, it should be generated or maintained separately from the baseline application's core business model.

---

 # 14\. Error Handling

 Implement conventional REST error handling.

 Use:

```
400 Bad Request
404 Not Found
409 Conflict
500 Internal Server Error
```

 Return a consistent error structure:

```
{
  "timestamp": "2026-09-01T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found",
  "path": "/api/customers/123"
}
```

 Use Spring's exception handling mechanisms rather than implementing error handling independently in every controller.

---

 # 15\. Validation

 Use Jakarta Bean Validation.

 Examples:

 - Required customer fields
- Valid email
- Positive transaction amount
- Required transaction type
- Required transaction status
- Valid currency
- Required support ticket subject
- Required support ticket description

 Return meaningful validation errors.

---

 # 16\. Observability

 Implement basic application logging.

 Log:

 - Request processing errors
- Transaction creation
- Support ticket creation
- Important state changes

 Do not log:

- Full sensitive customer information

 Optionally expose:

```
/actuator/health
```

 and basic Spring Boot Actuator health information.

 Do not add a full observability stack yet.

---

 # 17\. API Documentation

 Provide OpenAPI documentation.

 Swagger UI should expose:

 - Customer APIs
- Account APIs
- Transaction APIs
- Support ticket APIs
- Context APIs

 The API documentation should make it easy for any future service to understand how to consume the application.

---

 # 18\. Testing Requirements

 Provide tests for:

 ### Unit tests

 - Customer service
- Account service
- Transaction service
- Support ticket service

 ### Integration tests

 At minimum:

 - Customer creation
- Account creation
- Transaction creation
- Customer context retrieval
- Transaction context retrieval
- Support ticket creation

 The project should pass:

```
mvn test
```

 with all tests passing.

---

# 19\. README Requirements

 Create a comprehensive README explaining:

1. What the application does.
2. Architecture.
3. Technology stack.
4. How to run locally.
5. How to run tests.
6. API documentation.
7. Database schema.
8. Example API requests.

---

 # 20\. Definition of Done

 The project is complete when:

- [ ] Spring Boot application starts successfully.
- [ ] Flyway creates the database schema.
- [ ] Demo data is available.
- [ ] Customers can be created and retrieved.
- [ ] Accounts can be created and retrieved.
- [ ] Transactions can be created and retrieved.
- [ ] Support tickets can be created and retrieved.
- [ ] Customer context endpoint works.
- [ ] Transaction context endpoint works.
- [ ] REST validation works.
- [ ] REST error handling is consistent.
- [ ] OpenAPI documentation is available.
- [ ] Unit tests exist for core services.

---

 # 21\. Guiding Principle

 **Keep it boring.**

 The baseline should intentionally resemble an ordinary enterprise Spring Boot application.

 The success of the overall project will be measured not by how sophisticated the initial banking application is, but by how cleanly capabilities can be added without requiring a fundamental rewrite of the original application.
