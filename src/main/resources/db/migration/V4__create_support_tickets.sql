CREATE TYPE support_category AS ENUM ('ACCOUNT', 'TRANSACTION', 'PAYMENT', 'CARD', 'SECURITY', 'GENERAL');
CREATE TYPE support_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE support_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');

CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category support_category NOT NULL,
    priority support_priority NOT NULL DEFAULT 'MEDIUM',
    status support_status NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP
);

CREATE INDEX idx_support_tickets_customer_id ON support_tickets(customer_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);