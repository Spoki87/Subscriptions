CREATE TABLE IF NOT EXISTS app_user
(
    id       UUID PRIMARY KEY,
    username VARCHAR(255),
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    enabled  BOOLEAN      NOT NULL DEFAULT false,
    role     VARCHAR(50),
    currency VARCHAR(10)           DEFAULT 'PLN',
    CONSTRAINT uq_app_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS subscription
(
    id                 UUID PRIMARY KEY,
    name               VARCHAR(255),
    description        VARCHAR(255),
    price              NUMERIC(10, 2),
    currency           VARCHAR(10) DEFAULT 'PLN',
    subscription_model VARCHAR(50) DEFAULT 'MONTHLY',
    app_user_id        UUID REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_token
(
    id                  UUID PRIMARY KEY,
    token_hash          VARCHAR(255),
    expiry_date         TIMESTAMP,
    session_expiry_date TIMESTAMP,
    revoked             BOOLEAN NOT NULL DEFAULT false,
    app_user_id         UUID REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS registration_token
(
    id           UUID PRIMARY KEY,
    token        VARCHAR(255),
    created_time TIMESTAMP,
    expired_time TIMESTAMP,
    app_user_id  UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reset_password_token
(
    id           UUID PRIMARY KEY,
    token        VARCHAR(255),
    created_time TIMESTAMP,
    expired_time TIMESTAMP,
    app_user_id  UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_subscription_app_user_id ON subscription (app_user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_token_hash ON refresh_token (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_app_user_id ON refresh_token (app_user_id);
CREATE INDEX IF NOT EXISTS idx_registration_token_token ON registration_token (token);
CREATE INDEX IF NOT EXISTS idx_reset_password_token_token ON reset_password_token (token);
