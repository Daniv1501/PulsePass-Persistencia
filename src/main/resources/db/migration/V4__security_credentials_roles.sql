-- No se asignan contraseñas a los usuarios legados: requieren activación administrativa.
CREATE TABLE user_credentials (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    password_hash VARCHAR(100) NOT NULL,
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'STAFF', 'ADMIN')),
    PRIMARY KEY (user_id, role)
);
