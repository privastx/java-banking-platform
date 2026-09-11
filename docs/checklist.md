# Banking Platform Implementation Checklist

## Phase 1: Project Setup

- [ ] Initialize Maven project with Java 21
- [ ] Configure `pom.xml` with Spring Boot 3.x dependencies
  - [ ] Spring Web
  - [ ] Spring Data JPA
  - [ ] Hibernate
  - [ ] Bean Validation
  - [ ] PostgreSQL driver
  - [ ] Flyway
  - [ ] JUnit 5
  - [ ] Mockito
  - [ ] Spring Boot Test
  - [ ] OpenAPI/Swagger
- [ ] Create `application.yml` / `application.properties`
- [ ] Configure PostgreSQL connection
- [ ] Configure Flyway migration paths
- [ ] Set up package structure:
  - [ ] `customer/`
  - [ ] `account/`
  - [ ] `transaction/`
  - [ ] `support/`
  - [ ] `common/exception/`
  - [ ] `common/validation/`
  - [ ] `common/config/`

## Phase 2: Database Schema & Migrations

- [ ] Create `V1__create_customers.sql`
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] first_name, last_name, email (UNIQUE), phone, date_of_birth
  - [ ] country (ISO code), status (ENUM)
  - [ ] created_at, updated_at
  - [ ] NOT NULL constraints
- [ ] Create `V2__create_accounts.sql`
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] customer_id (FK to customers)
  - [ ] account_number (UNIQUE), account_type (ENUM), currency
  - [ ] balance (DECIMAL), status (ENUM)
  - [ ] created_at, updated_at
  - [ ] NOT NULL constraints
  - [ ] Index on customer_id
- [ ] Create `V3__create_transactions.sql`
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] account_id (FK to accounts)
  - [ ] transaction_type (ENUM), amount (DECIMAL), currency
  - [ ] merchant_name, merchant_category, country, city
  - [ ] transaction_timestamp, status, description
  - [ ] created_at
  - [ ] NOT NULL constraints
  - [ ] Index on account_id
  - [ ] Index on transaction_timestamp
  - [ ] Index on status
- [ ] Create `V4__create_support_tickets.sql`
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] customer_id (FK to customers)
  - [ ] subject, description, category (ENUM)
  - [ ] priority (ENUM), status (ENUM)
  - [ ] created_at, updated_at, resolved_at
  - [ ] NOT NULL constraints
  - [ ] Index on customer_id
  - [ ] Index on status
- [ ] Create `V5__insert_demo_data.sql`
  - [ ] 20 customers with varied data
  - [ ] 30 accounts distributed across customers
  - [ ] 500+ transactions with meaningful variation
    - [ ] Different amounts, merchants, categories
    - [ ] Different countries, cities, timestamps
    - [ ] Different types and statuses
    - [ ] Small number of unusual patterns (not labeled fraudulent)
  - [ ] 30+ support tickets across customers

## Phase 3: Domain Models

- [ ] Create `Customer` entity
  - [ ] JPA annotations
  - [ ] Status enum (ACTIVE, SUSPENDED, CLOSED)
  - [ ] @CreatedDate, @LastModifiedDate
  - [ ] Relationships: 1:N Account, 1:N SupportTicket
- [ ] Create `Account` entity
  - [ ] JPA annotations
  - [ ] AccountType enum (CHECKING, SAVINGS)
  - [ ] AccountStatus enum (ACTIVE, FROZEN, CLOSED)
  - [ ] BigDecimal balance
  - [ ] Relationship: many:1 Customer, 1:N Transaction
- [ ] Create `Transaction` entity
  - [ ] JPA annotations
  - [ ] TransactionType enum (PURCHASE, WITHDRAWAL, DEPOSIT, TRANSFER, PAYMENT, REFUND)
  - [ ] TransactionStatus enum (PENDING, COMPLETED, FAILED, REVERSED)
  - [ ] MerchantCategory enum (GROCERY, RESTAURANT, TRAVEL, etc.)
  - [ ] BigDecimal amount
  - [ ] Relationship: many:1 Account
- [ ] Create `SupportTicket` entity
  - [ ] JPA annotations
  - [ ] Category enum (ACCOUNT, TRANSACTION, PAYMENT, CARD, SECURITY, GENERAL)
  - [ ] Priority enum (LOW, MEDIUM, HIGH, URGENT)
  - [ ] Status enum (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
  - [ ] Relationship: many:1 Customer

## Phase 4: Repositories

- [ ] Create `CustomerRepository`
  - [ ] findByEmail
  - [ ] findById
  - [ ] findAll
- [ ] Create `AccountRepository`
  - [ ] findById
  - [ ] findByCustomerId
  - [ ] findByAccountNumber
  - [ ] findAll
- [ ] Create `TransactionRepository`
  - [ ] findById
  - [ ] findByAccountId
  - [ ] findByCustomerId (via account join)
  - [ ] findByStatus
  - [ ] findByTransactionTimestampBetween
  - [ ] Custom query methods for filtering
- [ ] Create `SupportTicketRepository`
  - [ ] findById
  - [ ] findByCustomerId
  - [ ] findByStatus
  - [ ] findAll

## Phase 5: DTOs

- [ ] Create `CustomerDto`
- [ ] Create `AccountDto`
- [ ] Create `TransactionDto`
- [ ] Create `SupportTicketDto`
- [ ] Create `CustomerContextDto` (for context endpoint)
- [ ] Create `TransactionContextDto` (for context endpoint)
- [ ] Create error response DTO

## Phase 6: Validation

- [ ] Create validation annotations/rules
  - [ ] Customer: required fields, valid email format
  - [ ] Account: required fields, valid currency
  - [ ] Transaction: positive amount, required type/status
  - [ ] SupportTicket: required subject and description
- [ ] Create validation config

## Phase 7: Services

- [ ] Implement `CustomerService`
  - [ ] Create customer
  - [ ] Get customer by ID
  - [ ] Update customer
  - [ ] Delete customer
  - [ ] Get customer accounts
  - [ ] Get customer transactions
  - [ ] Get customer support tickets
  - [ ] Get customer context
- [ ] Implement `AccountService`
  - [ ] Create account
  - [ ] Get account by ID
  - [ ] Update account
  - [ ] Delete account
  - [ ] Get account transactions
- [ ] Implement `TransactionService`
  - [ ] Create transaction
  - [ ] Get transaction by ID
  - [ ] Filter transactions (accountId, customerId, status, date range)
  - [ ] Get transaction context
- [ ] Implement `SupportTicketService`
  - [ ] Create ticket
  - [ ] Get ticket by ID
  - [ ] Update ticket
  - [ ] Get customer tickets

## Phase 8: Controllers

- [ ] Implement `CustomerController`
  - [ ] GET /api/customers
  - [ ] GET /api/customers/{id}
  - [ ] POST /api/customers
  - [ ] PUT /api/customers/{id}
  - [ ] DELETE /api/customers/{id}
  - [ ] GET /api/customers/{id}/accounts
  - [ ] GET /api/customers/{id}/transactions
  - [ ] GET /api/customers/{id}/support-tickets
  - [ ] GET /api/customers/{id}/context
- [ ] Implement `AccountController`
  - [ ] GET /api/accounts/{id}
  - [ ] POST /api/accounts
  - [ ] PUT /api/accounts/{id}
  - [ ] DELETE /api/accounts/{id}
  - [ ] GET /api/accounts/{id}/transactions
- [ ] Implement `TransactionController`
  - [ ] GET /api/transactions/{id}
  - [ ] POST /api/transactions
  - [ ] GET /api/transactions (with filtering)
  - [ ] GET /api/transactions/{id}/context
- [ ] Implement `SupportTicketController`
  - [ ] GET /api/support/tickets/{id}
  - [ ] POST /api/support/tickets
  - [ ] PUT /api/support/tickets/{id}
  - [ ] GET /api/customers/{id}/support-tickets

## Phase 9: Error Handling & Configuration

- [ ] Create global exception handler (`@RestControllerAdvice`)
  - [ ] 400 Bad Request (validation errors)
  - [ ] 404 Not Found
  - [ ] 409 Conflict (duplicate email/account number)
  - [ ] 500 Internal Server Error
  - [ ] Consistent error response structure
- [ ] Configure OpenAPI/Swagger
  - [ ] Customer APIs
  - [ ] Account APIs
  - [ ] Transaction APIs
  - [ ] Support ticket APIs
  - [ ] Context APIs
- [ ] Configure logging
  - [ ] Request processing errors
  - [ ] Transaction creation
  - [ ] Support ticket creation
  - [ ] Important state changes
  - [ ] Avoid logging sensitive customer info
- [ ] Configure Spring Boot Actuator (optional health endpoint)

## Phase 10: Testing

- [ ] Unit tests
  - [ ] CustomerService tests
  - [ ] AccountService tests
  - [ ] TransactionService tests
  - [ ] SupportTicketService tests
- [ ] Integration tests
  - [ ] Customer creation
  - [ ] Account creation
  - [ ] Transaction creation
  - [ ] Customer context retrieval
  - [ ] Transaction context retrieval
  - [ ] Support ticket creation
- [ ] Verify `mvn test` passes with all tests green

## Phase 11: Documentation

- [ ] Create `README.md`
  - [ ] What the application does
  - [ ] Architecture overview
  - [ ] Technology stack
  - [ ] How to run locally
  - [ ] How to run tests
  - [ ] API documentation reference
  - [ ] Database schema
  - [ ] Example API requests

## Phase 12: Final Verification

- [ ] Application starts successfully with `mvn spring-boot:run`
- [ ] Flyway creates database schema automatically
- [ ] Demo data is loaded
- [ ] All CRUD endpoints work for customers, accounts, transactions, support tickets
- [ ] Customer context endpoint returns consolidated view
- [ ] Transaction context endpoint returns fraud-detection-ready data
- [ ] REST validation works with meaningful error messages
- [ ] REST error handling is consistent across all endpoints
- [ ] OpenAPI/Swagger UI is accessible
- [ ] Unit tests exist and pass for core services
- [ ] Integration tests exist and pass
- [ ] `mvn clean install` succeeds
