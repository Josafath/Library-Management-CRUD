# Library-Management-CRUD

[![v2 (Shelfwise)](https://github.com/Josafath/Library-Management-CRUD/actions/workflows/v2.yml/badge.svg)](https://github.com/Josafath/Library-Management-CRUD/actions/workflows/v2.yml)

A desktop app to manage a small library, built with **Java Swing** and **MySQL** (JDBC).
The repository keeps both versions so the evolution can be compared:

| Folder | What it is |
|--------|------------|
| [`v1/`](v1) | The original NetBeans project, with its bugs and security problems fixed (described below). |
| [`v2/`](v2) | **Shelfwise**: a rewrite with loans, members, full CRUD, hashed passwords, a layered design, Maven, 87 tests and CI. See [v2/README.md](v2/README.md). |

---

## v1

There are two kinds of accounts:

| Role | What they can do |
|------|------------------|
| **Admin** | Add, list and delete staff (librarian) accounts |
| **Librarian** | Add, list and delete books (title, author, genre, status, publisher) |

```
Login (Principal)
 ├── role = admin      → AdminDashboard      → users table
 └── role = librarian  → LibrarianDashboard  → book table
```

### What changed in v1

The code and screens are the original ones; only these problems were fixed:

- **No more credentials in the code.** The MySQL password was hardcoded in `Principal.java`, and the admin username/password was hardcoded in the login check. Now the connection reads `db.properties` (ignored by git), and the admin is whoever has `role = 'admin'` in the `users` table.
- **Passwords are no longer printed** to the console when someone logs in.
- **Delete removed the wrong record.** It used "row number + 1" as the ID, so after any deletion it hit the wrong user/book (or none). It now reads the real ID from the table and asks for confirmation.
- **Book status bug.** The status combo had a blank third option that saved `2`, shown as "Occupied". It now only has *Occupied* (0) and *Available* (1).
- **Admins can't delete admin accounts**, so the system can't be left without an administrator.
- **Errors are shown to the user** (e.g. "That username already exists", or why the database connection failed) instead of only going to the log.
- **Resources are closed** (`try-with-resources`), and logging out closes the old connection instead of opening a new one each time.
- The broken `main()` methods in the dashboards (they always threw an exception) were removed.
- Added `schema.sql`, `db.properties.example` and `.gitignore`; compiled classes and NetBeans private settings are no longer in the repo.

### Requirements

- **JDK 21** or newer
- **MySQL 8**
- **MySQL Connector/J 8.4.0** (the JDBC driver)
- **Apache NetBeans** (optional; the project is a NetBeans/Ant project)

### Setup

#### 1. Install the tools

**Windows**

1. JDK 21: download the installer from [adoptium.net](https://adoptium.net) and check `java -version` in a new terminal.
2. MySQL: install [MySQL Installer for Windows](https://dev.mysql.com/downloads/installer/) (choose *Server* and, if you like, *Workbench*).
3. NetBeans (optional): [netbeans.apache.org](https://netbeans.apache.org/front/main/download/).

**macOS** (with [Homebrew](https://brew.sh))

```bash
brew install openjdk@21 mysql
brew services start mysql
brew install --cask netbeans   # optional
```

#### 2. Create the database

From the repository folder:

```bash
mysql -u root -p < v1/schema.sql
```

> On Windows, if `mysql` isn't recognized, use the full path, e.g.
> `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < v1\schema.sql`,
> or open `schema.sql` in MySQL Workbench and run it.

The script **drops and recreates** the `libreria` database and adds two demo accounts:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | admin |
| `librarian` | `librarian123` | librarian |

#### 3. Configure the connection

Copy the template and put your own MySQL user and password:

```bash
# macOS / Linux
cp v1/LibraryProject_/db.properties.example v1/LibraryProject_/db.properties
```

```bat
:: Windows
copy v1\LibraryProject_\db.properties.example v1\LibraryProject_\db.properties
```

`db.properties` is ignored by git, so the password stays on your machine.

#### 4. Add the JDBC driver

Download [`mysql-connector-j-8.4.0.jar`](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar)
and save it as `v1/LibraryProject_/lib/mysql-connector-j-8.4.0.jar`.

#### 5. Run it

**With NetBeans:** *File → Open Project* → `v1/LibraryProject_` → *Run* (F6).

**From a terminal** (inside `v1/LibraryProject_`, so the app finds `db.properties`):

```bash
# macOS / Linux
javac -d build/classes src/libraryproject_/*.java
java -cp "build/classes:lib/mysql-connector-j-8.4.0.jar" libraryproject_.LibraryProject_
```

```bat
:: Windows (classpath separator is ; instead of :)
javac -d build\classes src\libraryproject_\*.java
java -cp "build\classes;lib\mysql-connector-j-8.4.0.jar" libraryproject_.LibraryProject_
```

### Project structure

```
v1/
├── schema.sql                     # creates the libreria database + demo data
└── LibraryProject_/               # NetBeans project
    ├── db.properties.example      # template for your MySQL credentials
    ├── lib/                       # put the JDBC driver here (not in git)
    └── src/libraryproject_/
        ├── LibraryProject_.java   # entry point: opens the login
        ├── Database.java          # reads db.properties and opens the connection
        ├── Principal.java/.form   # login window
        ├── AdminDashboard.java/.form       # staff accounts CRUD
        └── LibrarianDashboard.java/.form   # books CRUD
```

The `.form` files belong to the NetBeans GUI builder; the code between the
`GEN-BEGIN`/`GEN-END` markers is generated from them, so edit the layout in NetBeans.

### Known limitations of v1

- Passwords are stored in plain text in the database.
- Records can be created and deleted, but not edited.
- There are no loans, searches or automated tests yet.
