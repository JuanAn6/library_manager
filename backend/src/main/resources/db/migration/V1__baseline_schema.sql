-- Baseline schema for the library manager.
-- Generated from the JPA entities under com.library.manager.model.
-- Tables are created parent-first so foreign keys resolve in order.

-- ---------------------------------------------------------------------------
-- Reference tables (no outgoing foreign keys)
-- ---------------------------------------------------------------------------

CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE publishers (
    id      BIGINT        NOT NULL AUTO_INCREMENT,
    name    VARCHAR(120)  NOT NULL,
    country VARCHAR(80),
    website VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_publishers_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE authors (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(255)  NOT NULL,
    last_name   VARCHAR(255)  NOT NULL,
    nationality VARCHAR(60),
    birth_date  DATE,
    biography   VARCHAR(2000),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE members (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    first_name      VARCHAR(255)  NOT NULL,
    last_name       VARCHAR(255)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    phone           VARCHAR(30),
    address         VARCHAR(255),
    membership_date DATE,
    active          BIT           NOT NULL,
    created_at      DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_members_email UNIQUE (email)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Books (references categories and publishers)
-- ---------------------------------------------------------------------------

CREATE TABLE books (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    title            VARCHAR(255)  NOT NULL,
    isbn             VARCHAR(20),
    category_id      BIGINT,
    publisher_id     BIGINT,
    language         VARCHAR(40),
    page_count       INT,
    description      VARCHAR(2000),
    cover_image_url  VARCHAR(500),
    published_date   DATE,
    total_copies     INT           NOT NULL DEFAULT 1,
    available_copies INT           NOT NULL DEFAULT 1,
    location         VARCHAR(255),
    available        BIT           NOT NULL,
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT fk_books_category  FOREIGN KEY (category_id)  REFERENCES categories (id),
    CONSTRAINT fk_books_publisher FOREIGN KEY (publisher_id) REFERENCES publishers (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Join table for the Book <-> Author many-to-many relationship
-- ---------------------------------------------------------------------------

CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_authors_book   FOREIGN KEY (book_id)   REFERENCES books (id),
    CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES authors (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Loans (references books and members)
-- ---------------------------------------------------------------------------

CREATE TABLE loans (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    book_id     BIGINT       NOT NULL,
    member_id   BIGINT       NOT NULL,
    loan_date   DATE         NOT NULL,
    due_date    DATE         NOT NULL,
    return_date DATE,
    status      VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_loans_book   FOREIGN KEY (book_id)   REFERENCES books (id),
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE = InnoDB;
