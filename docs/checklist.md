# Banking Platform Implementation Checklist

## Phase 1: Project Setup

- [x] Initialize Maven project with Java 21
- [x] Configure `pom.xml` with Spring Boot 3.x dependencies
  - [x] Spring Web
  - [x] Spring Data JPA
  - [x] Hibernate
  - [x] Bean Validation
  - [x] PostgreSQL driver
  - [x] Flyway
  - [x] JUnit 5
  - [x] Mockito
  - [x] Spring Boot Test
  - [x] OpenAPI/Swagger
- [x] Create `application.yml` / `application.properties`
- [x] Configure PostgreSQL connection
- [x] Configure Flyway migration paths
- [x] Set up package structure:
  - [x] `customer/`
  - [x] `account/`
  - [x] `transaction/`
  - [x] `support/`
  - [x] `common/exception/`
  - [x] `common/validation/`
  - [x] `common/config/`

## Phase 2: Database Schema & Migrations

- [x] Create `V1__create_customers.sql`
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] first_name, last_name, email (UNIQUE), phone, date_of_birth
  - [x] country (ISO code), status (ENUM)
  - [x] created_at, updated_at
  - [x] NOT NULL constraints
- [x] Create `V2__create_accounts.sql`
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] customer_id (FK to customers)
  - [x] account_number (UNIQUE), account_type (ENUM), currency
  - [x] balance (DECIMAL), status (ENUM)
  - [x] created_at, updated_at
  - [x] NOT NULL constraints
  - [x] Index on customer_id
- [x] Create `V3__create_transactions.sql`
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] account_id (FK to accounts)
  - [x] transaction_type (ENUM), amount (DECIMAL), currency
  - [x] merchant_name, merchant_category, country, city
  - [x] transaction_timestamp, status, description
  - [x] created_at
  - [x] NOT NULL constraints
  - [x] Index on account_id
  - [x] Index on transaction_timestamp
  - [x] Index on status
- [x] Create `V4__create_support_tickets.sql`
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] customer_id (FK to customers)
  - [x] subject, description, category (ENUM)
  - [x] priority (ENUM), status (ENUM)
  - [x] created_at, updated_at, resolved_at
  - [x] NOT NULL constraints
  - [x] Index on customer_id
  - [x] Index on status
- [x] Create `V5__insert_demo_data.sql`
  - [x] 20 customers with varied data
  - [x] 30 accounts distributed across customers
  - [x] 500+ transactions with meaningful variation
    - [x] Different amounts, merchants, categories
    - [x] Different countries, cities, timestamps
    - [x] Different types and statuses
    - [x] Small number of unusual patterns (not labeled fraudulent)
  - [x] 30+ support tickets across customers

## Phase 3: Domain Models

- [x] Create `Customer` entity
  - [x] JPA annotations
  - [x] Status enum (ACTIVE, SUSPENDED, CLOSED)
  - [x] @CreatedDate, @LastModifiedDate
  - [x] Relationships: 1:N Account, 1:N SupportTicket
- [x] Create `Account` entity
  - [x] JPA annotations
  - [x] AccountType enum (CHECKING, SAVINGS)
  - [x] AccountStatus enum (ACTIVE, FROZEN, CLOSED)
  - [x] BigDecimal balance
  - [x] Relationship: many:1 Customer, 1:N Transaction
- [x] Create `Transaction` entity
  - [x] JPA annotations
  - [x] TransactionType enum (PURCHASE, WITHDRAWAL, DEPOSIT, TRANSFER, PAYMENT, REFUND)
  - [x] TransactionStatus enum (PENDING, COMPLETED, FAILED, REVERSED)
  - [x] MerchantCategory enum (GROCERY, RESTAURANT, TRAVEL, etc.)
  - [x] BigDecimal amount
  - [x] Relationship: many:1 Account
- [x] Create `SupportTicket` entity
  - [x] JPA annotations
  - [x] Category enum (ACCOUNT, TRANSACTION, PAYMENT, CARD, SECURITY, GENERAL)
  - [x] Priority enum (LOW, MEDIUM, HIGH, URGENT)
  - [x] Status enum (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
  - [x] Relationship: many:1 Customer

## Phase 4: Repositories

- [x] Create `CustomerRepository`
  - [x] findByEmail
  - [x] findById
  - [x] findAll
- [x] Create `AccountRepository`
  - [x] findById
  - [x] findByCustomerId
  - [x] findByAccountNumber
  - [x] findAll
- [x] Create `TransactionRepository`
  - [x] findById
  - [x] findByAccountId
  - [x] findByCustomerId (via account join)
  - [x] findByStatus
  - [x] findByTransactionTimestampBetween
  - [x] Custom query methods for filtering
- [x] Create `SupportTicketRepository`
  - [x] findById
  - [x] findByCustomerId
  - [x] findByStatus
  - [x] findAll

## Phase 5: DTOs

- [x] Create `CustomerDto`
- [x] Create `AccountDto`
- [x] Create `TransactionDto`
- [x] Create `SupportTicketDto`
- [x] Create `CustomerContextDto` (for context endpoint)
- [x] Create `TransactionContextDto` (for context endpoint)
- [x] Create error response DTO

## Phase 6: Validation

- [x] Create validation annotations/rules
  - [x] Customer: required fields, valid email format
  - [x] Account: required fields, valid currency
  - [x] Transaction: positive amount, required type/status
  - [x] SupportTicket: required subject and description
- [x] Create validation config

## Phase 7: Services

- [x] Implement `CustomerService`
  - [x] Create customer
  - [x] Get customer by ID
  - [x] Update customer
  - [x] Delete customer
  - [x] Get customer accounts
  - [x] Get customer transactions
  - [x] Get customer support tickets
  - [x] Get customer context
- [x] Implement `AccountService`
  - [x] Create account
  - [x] Get account by ID
  - [x] Update account
  - [x] Delete account
  - [x] Get account transactions
- [x] Implement `TransactionService`
  - [x] Create transaction
  - [x] Get transaction by ID
  - [x] Filter transactions (accountId, customerId, status, date range)
  - [x] Get transaction context
- [x] Implement `SupportTicketService`
  - [x] Create ticket
  - [x] Get ticket by ID
  - [x] Update ticket
  - [x] Get customer tickets

## Phase 8: Controllers

- [x] Implement `CustomerController`
  - [x] GET /api/customers
  - [x] GET /api/customers/{id}
  - [x] POST /api/customers
  - [x] PUT /api/customers/{id}
  - [x] DELETE /api/customers/{id}
  - [x] GET /api/customers/{id}/accounts
  - [x] GET /api/customers/{id}/transactions
  - [x] GET /api/customers/{id}/support-tickets
  - [x] GET /api/customers/{id}/context
- [x] Implement `AccountController`
  - [x] GET /api/accounts/{id}
  - [x] POST /api/accounts
  - [x] PUT /api/accounts/{id}
  - [x] DELETE /api/accounts/{id}
  - [x] GET /api/accounts/{id}/transactions
- [x] Implement `TransactionController`
  - [x] GET /api/transactions/{id}
  - [x] POST /api/transactions
  - [x] GET /api/transactions (with filtering)
  - [x] GET /api/transactions/{id}/context
- [x] Implement `SupportTicketController`
  - [x] GET /api/support/tickets/{id}
  - [x] POST /api/support/tickets
  - [x] PUT /api/support/tickets/{id}
  - [x] GET /api/customers/{id}/support-tickets

## Phase 9: Error Handling & Configuration

- [x] Create global exception handler (`@RestControllerAdvice`)
  - [x] 400 Bad Request (validation errors)
  - [x] 404 Not Found
  - [x] 409 Conflict (duplicate email/account number)
  - [x] 500 Internal Server Error
  - [x] Consistent error response structure
- [x] Configure OpenAPI/Swagger
  - [x] Customer APIs
  - [x] Account APIs
  - [x] Transaction APIs
  - [x] Support ticket APIs
  - [x] Context APIs
- [x] Configure logging
  - [x] Request processing errors
  - [x] Transaction creation
  - [x] Support ticket creation
  - [x] Important state changes
  - [x] Avoid logging sensitive customer info
- [x] Configure Spring Boot Actuator (optional health endpoint)

## Phase 10: Testing

- [x] Unit tests
  - [x] CustomerService tests
  - [x] AccountService tests
  - [x] TransactionService tests
  - [x] SupportTicketService tests
- [x] Integration tests
  - [x] Customer creation
  - [x] Account creation
  - [x] Transaction creation
  - [x] Customer context retrieval
  - [x] Transaction context retrieval
  - [x] Support ticket creation
- [ ] Verify `mvn test` passes with all tests green

## Phase 11: Documentation

- [x] Create `README.md`
  - [x] What the application does
  - [x] Architecture overview
  - [x] Technology stack
  - [x] How to run locally
  - [x] How to run tests
  - [x] API documentation reference
  - [x] Database schema
  - [x] Example API requests

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
