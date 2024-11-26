ALTER TABLE users
    MODIFY provider_id VARCHAR(255) NULL,
    MODIFY email VARCHAR(255) NULL,
    MODIFY customer_key BINARY(16) NULL;
