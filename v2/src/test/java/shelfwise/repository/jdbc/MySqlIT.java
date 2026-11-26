package shelfwise.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelfwise.Services;
import shelfwise.config.AppConfig;
import shelfwise.db.ConnectionFactory;
import shelfwise.db.SchemaInstaller;
import shelfwise.model.Book;
import shelfwise.model.Loan;
import shelfwise.model.Member;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.security.PasswordHasher;
import shelfwise.service.LoanService;
import shelfwise.service.ValidationException;
import shelfwise.support.MutableClock;

/**
 * Runs the real SQL against a real MySQL server.
 *
 * Needs a database that can be wiped (every test starts from empty tables):
 *
 * <pre>
 * SHELFWISE_TEST_DB_URL=jdbc:mysql://localhost:3306/shelfwise_test?createDatabaseIfNotExist=true
 * SHELFWISE_TEST_DB_USER=root
 * SHELFWISE_TEST_DB_PASSWORD=...
 * ./mvnw verify
 * </pre>
 *
 * Without SHELFWISE_TEST_DB_URL these tests are skipped.
 */
class MySqlIT {

    private static ConnectionFactory connections;

    private final MutableClock clock = new MutableClock(LocalDate.of(2026, 3, 1));
    private Services services;

    @BeforeAll
    static void connect() {
        String url = System.getenv("SHELFWISE_TEST_DB_URL");
        assumeTrue(url != null && !url.isBlank(), "SHELFWISE_TEST_DB_URL not set: skipping MySQL tests");
        String user = System.getenv().getOrDefault("SHELFWISE_TEST_DB_USER", "root");
        String password = System.getenv().getOrDefault("SHELFWISE_TEST_DB_PASSWORD", "");
        connections = ConnectionFactory.fromConfig(new AppConfig(url, user, password));
    }

    @BeforeEach
    void emptyDatabase() throws SQLException {
        // Drop in reverse dependency order, then let the installer recreate everything
        try (Connection conn = connections.open(); Statement stmt = conn.createStatement()) {
            for (String table : List.of("loans", "books", "members", "users")) {
                stmt.execute("DROP TABLE IF EXISTS " + table);
            }
        }
        SchemaInstaller.install(connections);
        SchemaInstaller.install(connections);   // a second run must change nothing
        services = Services.wire(new JdbcUserRepository(connections), new JdbcBookRepository(connections),
                new JdbcMemberRepository(connections), new JdbcLoanRepository(connections),
                new PasswordHasher(1_000), clock);
    }

    @Test
    void usersRoundTripWithHashedPasswords() {
        assertTrue(services.auth().needsSetup());
        User admin = services.auth().createFirstAdmin("Ana", "Muñoz", 30, "admin", "contraseña-ñ".toCharArray());
        assertFalse(services.auth().needsSetup());

        User logged = services.auth().login("ADMIN", "contraseña-ñ".toCharArray());
        assertEquals(admin.id(), logged.id());
        assertEquals("Muñoz", logged.lastName());
        assertEquals(Role.ADMIN, logged.role());
        assertTrue(logged.passwordHash().startsWith("pbkdf2_sha256$"));

        User luis = services.users().create("Luis", "Pérez", 25, "luis", "librarian-pass".toCharArray(), Role.LIBRARIAN);
        services.users().update(luis.id(), "Luis", "Pérez", 26, "luisp", Role.LIBRARIAN, admin);
        assertEquals("luisp", services.auth().login("luisp", "librarian-pass".toCharArray()).username());

        // UNIQUE(username) in MySQL becomes a friendly message
        ValidationException dup = assertThrows(ValidationException.class, () ->
                services.users().create("X", "Y", 30, "LuisP", "whatever-pass".toCharArray(), Role.LIBRARIAN));
        assertTrue(dup.getMessage().contains("already taken"));

        services.users().delete(luis.id(), admin);
        assertEquals(1, services.users().list().size());
    }

    @Test
    void booksSearchUpdateAndDelete() {
        Book cien = services.books().save(Book.draft("Cien años de soledad", "Gabriel García Márquez", "Novel", "Sudamericana"));
        services.books().save(Book.draft("100% Java", "Someone", "Programming", "Pub"));
        services.books().save(Book.draft("1000 recipes", "Chef", "Cooking", "Pub"));

        assertEquals(1, services.books().search("GARCÍA").size());
        assertEquals(1, services.books().search("años").size());
        // "%" typed by the user is literal: only "100% Java" matches, not "1000 recipes"
        assertEquals(List.of("100% Java"), services.books().search("100%").stream().map(Book::title).toList());
        assertEquals(3, services.books().search("").size());

        services.books().save(new Book(cien.id(), "Cien años de soledad", "G. García Márquez", "Novel", "Sudamericana", true));
        assertEquals("G. García Márquez", services.books().search("soledad").get(0).author());

        services.books().delete(cien.id());
        assertEquals(2, services.books().search("").size());
    }

    @Test
    void membersHaveUniqueEmails() {
        services.members().save(Member.draft("Ana López", "ana@example.com", "555-0101"));
        assertThrows(ValidationException.class,
                () -> services.members().save(Member.draft("Otra", "ana@example.com", "")));
        assertEquals(1, services.members().search("0101").size());
    }

    @Test
    void loansFlowAgainstTheRealConstraints() {
        Book book = services.books().save(Book.draft("1984", "George Orwell", "Dystopia", "Secker & Warburg"));
        Member ana = services.members().save(Member.draft("Ana", "ana@example.com", ""));
        Member carlos = services.members().save(Member.draft("Carlos", "carlos@example.com", ""));

        Loan loan = services.loans().checkout(book.id(), ana.id(), 7);
        assertEquals(LocalDate.of(2026, 3, 8), loan.dueDate());
        assertFalse(services.books().search("1984").get(0).available());
        assertTrue(services.books().available().isEmpty());

        // The UNIQUE(active_book_id) index refuses a second active loan, even bypassing the service
        JdbcLoanRepository repo = new JdbcLoanRepository(connections);
        assertThrows(DuplicateKeyException.class, () -> repo.insert(
                new Loan(0, book.id(), carlos.id(), null, null, clock.instant().atZone(clock.getZone()).toLocalDate(),
                        LocalDate.of(2026, 3, 10), null)));

        clock.advanceDays(10);
        List<Loan> overdue = services.loans().list(LoanService.Filter.OVERDUE);
        assertEquals(1, overdue.size());
        assertEquals("1984", overdue.get(0).bookTitle());
        assertEquals("Ana", overdue.get(0).memberName());

        services.loans().returnBook(loan.id());
        assertTrue(services.books().search("1984").get(0).available());
        assertEquals(LocalDate.of(2026, 3, 11), repo.findById(loan.id()).orElseThrow().returnDate());

        // After the return the same book can be lent again
        services.loans().checkout(book.id(), carlos.id(), 14);
        assertEquals(2, services.loans().list(LoanService.Filter.ALL).size());
        assertEquals(1, services.loans().list(LoanService.Filter.ACTIVE).size());
    }

    @Test
    void deletingABookAlsoDeletesItsReturnedLoans() {
        Book book = services.books().save(Book.draft("1984", "George Orwell", "Dystopia", "Secker"));
        Member ana = services.members().save(Member.draft("Ana", "ana@example.com", ""));
        Loan loan = services.loans().checkout(book.id(), ana.id(), 7);

        assertThrows(ValidationException.class, () -> services.books().delete(book.id()));   // still out

        services.loans().returnBook(loan.id());
        services.books().delete(book.id());   // ON DELETE CASCADE
        assertTrue(services.loans().list(LoanService.Filter.ALL).isEmpty());
    }
}
