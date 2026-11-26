# Shelfwise (v2)

A rewrite of the [v1 library app](../v1) as a small but complete library system:
**Java 21 + Swing + MySQL**, built with Maven, with tests and CI.

| | v1 | v2 (Shelfwise) |
|---|---|---|
| Features | Add/delete staff and books | Full CRUD (edit included) for books, members and staff, **loans and returns**, overdue tracking, search |
| Passwords | Plain text in the database | **PBKDF2-SHA256** hashes (600,000 rounds, random salt) |
| Admin account | Hardcoded in the source code | Created by a **first-run wizard**; role stored per account |
| Login | Unlimited attempts | Locked for 1 minute after 5 failures; same message for unknown user and wrong password |
| Book status | Typed by hand (could disagree with reality) | **Calculated from the loans** |
| Database setup | Run `schema.sql` by hand | Tables created automatically on start |
| Code structure | SQL inside the window classes | Layers: UI → services → repositories → JDBC |
| UI code | NetBeans GUI-builder output | Hand-written Swing, one reusable CRUD screen |
| Build | NetBeans/Ant, absolute JAR path | Maven wrapper (`./mvnw`), nothing to install but Java |
| Tests | None | 82 unit tests + 5 MySQL integration tests, CI on Windows/macOS/Linux |
| Dependencies | MySQL driver | MySQL driver only (JUnit for tests) |

## Screens

- **Books**: catalog with live search (title, author, genre, publisher). Status shows *Available* or *On loan*.
- **Members**: the people who borrow books (name, unique email, optional phone).
- **Loans**: lend a book (choose book, member and days), register returns, and filter by *Active*, *Overdue* or *All*. Overdue loans are highlighted in red with their days of delay.
- **Staff** (admins only): create, edit and delete accounts, and reset passwords.
- Every user can **change their own password** from the header.

### Business rules

- A book can't be lent while it is already out. MySQL enforces this too, with a unique index, so two librarians can't lend the same copy at the same time.
- A member can have at most **3** books at once. Loans last 1–60 days (14 by default).
- Books and members with an active loan can't be deleted. Once everything is returned, deleting them also removes their loan history.
- There is always at least one admin: nobody can delete their own account or remove their own admin role, and the last admin can't be deleted.

## Architecture

```
ui/            Swing windows. They only call services; no SQL here.
   CrudPanel   one generic "search + table + form" screen reused by Books, Members and Staff
service/       business rules and validation (ValidationException = message for the user)
repository/    interfaces: what the services need from storage
   jdbc/       MySQL implementation (PreparedStatement + try-with-resources everywhere)
security/      PasswordHasher (PBKDF2) and LoginThrottle (lockout)
model/         immutable records: User, Book, Member, Loan, Role
db/, config/   connection settings (db.properties, read as UTF-8) and schema installer
```

Services depend on the repository **interfaces**, not on JDBC. The unit tests take advantage of that: they run the real services on top of in-memory repositories (`src/test/.../InMemoryRepositories.java`) that copy the database rules. The integration tests (`MySqlIT`) then run the same flows against a real MySQL server.

### How passwords are stored

```
pbkdf2_sha256 $ 600000 $ <16-byte random salt, Base64> $ <32-byte hash, Base64>
```

- The **salt** makes two equal passwords produce different hashes, so precomputed tables are useless.
- The **iteration count** makes every guess expensive: 600,000 rounds is OWASP's recommendation for PBKDF2-SHA256. It is stored in each hash, so it can be raised later without breaking old accounts.
- Hashes are compared with `MessageDigest.isEqual`, which takes the same time wherever the bytes differ.
- When a username doesn't exist, the login still checks a dummy hash. That way the response time doesn't reveal which usernames are real.

### How "available" works

There is no status column. A book is available when it has no loan with `return_date IS NULL`:

```sql
NOT EXISTS (SELECT 1 FROM loans l WHERE l.book_id = b.id AND l.return_date IS NULL) AS available
```

The `loans` table also has a generated column, `active_book_id`, which equals `book_id` while the loan is open and `NULL` after the return. Its `UNIQUE` index stops MySQL from accepting a second open loan of the same book.

## Requirements

- **JDK 21** or newer
- **MySQL 8**

Maven does **not** need to be installed: `mvnw` / `mvnw.cmd` download it the first time.

## Setup

### 1. Install the tools

**Windows**

1. JDK 21: install it from [adoptium.net](https://adoptium.net), then check with `java -version` in a new terminal.
2. MySQL 8: install it with [MySQL Installer](https://dev.mysql.com/downloads/installer/) (choose *Server*).

**macOS** (with [Homebrew](https://brew.sh))

```bash
brew install openjdk@21 mysql
brew services start mysql
```

### 2. Configure the connection

Inside the `v2` folder, copy the template and put your MySQL user and password in it:

```bash
# macOS / Linux
cd v2
cp db.properties.example db.properties
```

```bat
:: Windows
cd v2
copy db.properties.example db.properties
```

Then edit `db.properties`. It is ignored by git, so your password stays on your machine, and it is read as UTF-8, so passwords with `ñ` or accents work.

You don't need to run any SQL. On the first start the app creates the `shelfwise` database and its tables. For that, the user needs permission to create databases; `root` has it.

### 3. Run

```bash
# macOS / Linux
./mvnw package -DskipTests
java -jar target/shelfwise.jar
```

```bat
:: Windows
mvnw.cmd package -DskipTests
java -jar target\shelfwise.jar
```

Run it from the `v2` folder, so the app finds `db.properties`. To use a config file somewhere else, pass `java -Dshelfwise.config=C:\path\db.properties -jar target/shelfwise.jar`.

The first time, a wizard asks you to create the **admin account** and offers to add sample books and members. After that you sign in with that account.

**NetBeans / IntelliJ:** open the `v2` folder as a Maven project and run `shelfwise.App`.

## Tests

```bash
./mvnw test        # 82 unit tests, no database needed
./mvnw verify      # + integration tests (skipped unless the variables below are set)
```

The integration tests **drop and recreate the tables** of the database you point them to, so use a separate one:

```bash
# macOS / Linux
export SHELFWISE_TEST_DB_URL="jdbc:mysql://localhost:3306/shelfwise_test?createDatabaseIfNotExist=true"
export SHELFWISE_TEST_DB_USER=root
export SHELFWISE_TEST_DB_PASSWORD='your-password'
./mvnw verify
```

```bat
:: Windows (cmd)
set SHELFWISE_TEST_DB_URL=jdbc:mysql://localhost:3306/shelfwise_test?createDatabaseIfNotExist=true
set SHELFWISE_TEST_DB_USER=root
set SHELFWISE_TEST_DB_PASSWORD=your-password
mvnw.cmd verify
```

GitHub Actions ([`.github/workflows/v2.yml`](../.github/workflows/v2.yml)) runs the unit tests on Windows, macOS and Linux, and the integration tests against a MySQL 8.4 container.

## Project structure

```
v2/
├── pom.xml                      # dependencies: mysql-connector-j (runtime), JUnit (tests)
├── mvnw, mvnw.cmd, .mvn/        # Maven wrapper
├── db.properties.example        # template for your MySQL credentials
└── src/
    ├── main/java/shelfwise/
    │   ├── App.java             # start-up: config → schema → first-run wizard → login
    │   ├── Services.java        # wires repositories and services together
    │   ├── SampleData.java      # optional demo books and members
    │   ├── config/  db/  model/  repository/  repository/jdbc/  security/  service/  ui/
    ├── main/resources/shelfwise/schema.sql
    └── test/java/shelfwise/     # unit tests, in-memory repositories, MySqlIT
```

## Known limitations

- Each book row is a single copy. Several copies of the same title are added as separate books.
- Database calls run on the Swing thread. With a local MySQL server this is instant, but a slow remote server would freeze the window briefly.
- The login lockout lives in memory, so restarting the app clears it.
