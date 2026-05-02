CREATE TABLE wallets (
                         id BIGSERIAL PRIMARY KEY,

                         user_id BIGINT NOT NULL UNIQUE,

                         balance NUMERIC(10, 2) NOT NULL DEFAULT 0.00,

                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT chk_wallet_balance_non_negative
                             CHECK (balance >= 0)
);

CREATE TABLE wallet_transactions (
                                     id BIGSERIAL PRIMARY KEY,

                                     wallet_id BIGINT NOT NULL,
                                     user_id BIGINT NOT NULL,

                                     type VARCHAR(50) NOT NULL,
                                     status VARCHAR(50) NOT NULL,

                                     amount NUMERIC(10, 2) NOT NULL,
                                     balance_after NUMERIC(10, 2) NOT NULL,

                                     source_service VARCHAR(100),
                                     source_reference_id VARCHAR(255),

                                     description TEXT,

                                     created_at TIMESTAMP NOT NULL,

                                     CONSTRAINT fk_wallet_transaction_wallet
                                         FOREIGN KEY (wallet_id)
                                             REFERENCES wallets(id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT chk_wallet_transaction_type
                                         CHECK (type IN (
                                                         'AUDIO_REWARD',
                                                         'MANUAL_ADJUSTMENT',
                                                         'WITHDRAWAL_REQUEST',
                                                         'WITHDRAWAL_CANCELLED'
                                             )),

                                     CONSTRAINT chk_wallet_transaction_status
                                         CHECK (status IN (
                                                           'PENDING',
                                                           'COMPLETED',
                                                           'REJECTED',
                                                           'CANCELLED'
                                             )),

                                     CONSTRAINT chk_wallet_transaction_amount_positive
                                         CHECK (amount > 0),

                                     CONSTRAINT chk_wallet_transaction_balance_after_non_negative
                                         CHECK (balance_after >= 0)
);

CREATE UNIQUE INDEX uk_wallet_transactions_source_reference
    ON wallet_transactions(source_service, source_reference_id)
    WHERE source_service IS NOT NULL
      AND source_reference_id IS NOT NULL;

CREATE INDEX idx_wallet_transactions_user_id
    ON wallet_transactions(user_id);

CREATE INDEX idx_wallet_transactions_wallet_id
    ON wallet_transactions(wallet_id);

CREATE INDEX idx_wallet_transactions_status
    ON wallet_transactions(status);

CREATE INDEX idx_wallet_transactions_created_at
    ON wallet_transactions(created_at);