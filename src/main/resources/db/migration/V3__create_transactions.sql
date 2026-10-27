CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    transaction_type VARCHAR NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    merchant_name VARCHAR(255),
    merchant_category VARCHAR,
    country VARCHAR(10),
    city VARCHAR(100),
    transaction_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_timestamp ON transactions(transaction_timestamp);
CREATE INDEX idx_transactions_status ON transactions(status);