-- Accounts that can sign in to the application.
-- These are NOT the library members: a member is someone who borrows books and
-- has no password, while a user is someone who operates the application.

-- One row per role. A user holds exactly one of these.
CREATE TABLE roles (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(40) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB;

-- ADMIN and MANAGER may change the catalogue; MEMBER can only read it.
INSERT INTO roles (name, created_at, updated_at) VALUES
    ('ADMIN',   NOW(6), NOW(6)),
    ('MANAGER', NOW(6), NOW(6)),
    ('MEMBER',  NOW(6), NOW(6));

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(80)  NOT NULL,
    -- BCrypt hashes are 60 characters, plus the "{bcrypt}" prefix the
    -- delegating encoder writes. 100 leaves room to migrate algorithms later.
    password_hash VARCHAR(100) NOT NULL,
    enabled       BIT          NOT NULL DEFAULT 1,
    role_id       BIGINT       NOT NULL,
    created_at    DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;
