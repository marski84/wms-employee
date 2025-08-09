CREATE TABLE IF NOT EXISTS employee
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    nickname   VARCHAR(100),
    username   VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auth0_users_email ON employee (email);
CREATE INDEX IF NOT EXISTS idx_auth0_users_username ON employee (username);