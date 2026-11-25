-- Database used by v1 (Java Swing + MySQL).
-- Run it once:  mysql -u root -p < v1/schema.sql
-- WARNING: it drops and recreates the "libreria" database.

DROP DATABASE IF EXISTS libreria;
CREATE DATABASE libreria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE libreria;

-- Staff accounts. "role" replaces the admin username/password that used to be
-- hardcoded in the login screen.
-- v1 still stores passwords in plain text (hashing comes in v2).
CREATE TABLE users (
    idusers   INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(60)  NOT NULL,
    last_name VARCHAR(60)  NOT NULL,
    age       INT          NOT NULL,
    username  VARCHAR(40)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      ENUM('admin', 'librarian') NOT NULL DEFAULT 'librarian'
);

-- Book catalog. status: 1 = Available, 0 = Occupied.
CREATE TABLE book (
    idbook    INT AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR(150) NOT NULL,
    author    VARCHAR(100) NOT NULL,
    genre     VARCHAR(60)  NOT NULL,
    status    TINYINT(1)   NOT NULL DEFAULT 1,
    editorial VARCHAR(100) NOT NULL
);

-- Demo accounts (change them if you deploy this anywhere real)
INSERT INTO users (name, last_name, age, username, password, role) VALUES
    ('Demo', 'Admin',     30, 'admin',     'admin123',     'admin'),
    ('Demo', 'Librarian', 25, 'librarian', 'librarian123', 'librarian');

INSERT INTO book (title, author, genre, status, editorial) VALUES
    ('Cien años de soledad', 'Gabriel García Márquez', 'Novel',       1, 'Sudamericana'),
    ('El Principito',        'Antoine de Saint-Exupéry', 'Fable',     1, 'Reynal & Hitchcock'),
    ('Clean Code',           'Robert C. Martin',       'Programming', 0, 'Prentice Hall');
