CREATE TYPE transaction_type AS ENUM ('PURCHASE', 'WITHDRAWAL', 'DEPOSIT', 'TRANSFER', 'PAYMENT', 'REFUND');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED');
CREATE TYPE merchant_category AS ENUM ('GROCERY', 'RESTAURANT', 'TRAVEL', 'ELECTRONICS', 'ENTERTAINMENT', 'HEALTHCARE', 'GAS_STATION', 'ONLINE_RETAIL', 'OTHER');

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    transaction_type transaction_type NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    merchant_name VARCHAR(255),
    merchant_category merchant_category,
    country VARCHAR(10),
    city VARCHAR(100),
    transaction_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    status transaction_status NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_timestamp ON transactions(transaction_timestamp);
CREATE INDEX idx_transactions_status ON transactions(status);