-- Shelfwise (v2) schema. The app runs this file on every start;
-- "IF NOT EXISTS" makes that safe on a database that already has data.

-- Staff accounts. password_hash holds a PBKDF2 hash, never the password.
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(60)  NOT NULL,
    last_name     VARCHAR(60)  NOT NULL,
    age           INT          NOT NULL,
    username      VARCHAR(40)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'LIBRARIAN') NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Book catalog. There is no "status" column: availability comes from loans.
CREATE TABLE IF NOT EXISTS books (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(150) NOT NULL,
    author     VARCHAR(100) NOT NULL,
    genre      VARCHAR(60)  NOT NULL,
    publisher  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- People who borrow books.
CREATE TABLE IF NOT EXISTS members (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(120) NOT NULL,
    email      VARCHAR(120) NOT NULL,
    phone      VARCHAR(30)  NOT NULL DEFAULT '',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_members_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Loans. return_date stays NULL while the book is out.
-- active_book_id equals book_id only while the loan is active (NULL after
-- the return). Its UNIQUE index lets MySQL itself refuse a second active
-- loan of the same book, even if two librarians click at the same time.
-- Deleting a book or member also deletes its (returned) loan history; the
-- app refuses to delete either while a loan is still active.
CREATE TABLE IF NOT EXISTS loans (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id        BIGINT NOT NULL,
    member_id      BIGINT NOT NULL,
    loan_date      DATE   NOT NULL,
    due_date       DATE   NOT NULL,
    return_date    DATE   NULL,
    active_book_id BIGINT GENERATED ALWAYS AS (IF(return_date IS NULL, book_id, NULL)) VIRTUAL,
    CONSTRAINT uq_loans_active_book UNIQUE (active_book_id),
    CONSTRAINT fk_loans_book   FOREIGN KEY (book_id)   REFERENCES books (id)   ON DELETE CASCADE,
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT ck_loans_dates  CHECK (due_date >= loan_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
