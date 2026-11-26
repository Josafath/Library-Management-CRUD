package shelfwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelfwise.Services;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class AuthServiceTest {

    private final MutableClock clock = new MutableClock(LocalDate.of(2026, 3, 1));
    private InMemoryRepositories db;
    private Services services;

    @BeforeEach
    void setUp() {
        db = new InMemoryRepositories();
        services = db.services(clock);
    }

    private User createAdmin() {
        return services.auth().createFirstAdmin("Ana", "López", 30, "admin", "admin-pass".toCharArray());
    }

    @Test
    void freshDatabaseNeedsSetup() {
        assertTrue(services.auth().needsSetup());
        createAdmin();
        assertFalse(services.auth().needsSetup());
    }

    @Test
    void firstAdminCanOnlyBeCreatedOnce() {
        createAdmin();
        ValidationException ex = assertThrows(ValidationException.class,
                () -> services.auth().createFirstAdmin("X", "Y", 30, "intruder", "intruder-pass".toCharArray()));
        assertEquals("An administrator already exists.", ex.getMessage());
    }

    @Test
    void firstAdminIsStoredWithAHashNotThePassword() {
        User admin = createAdmin();
        assertEquals(Role.ADMIN, admin.role());
        assertFalse(db.users.findById(admin.id()).orElseThrow().passwordHash().contains("admin-pass"));
    }

    @Test
    void loginWithCorrectCredentialsReturnsTheUser() {
        User admin = createAdmin();
        User logged = services.auth().login("admin", "admin-pass".toCharArray());
        assertEquals(admin.id(), logged.id());
    }

    @Test
    void usernameIsTrimmedAndCaseInsensitive() {
        createAdmin();
        assertEquals("admin", services.auth().login("  ADMIN ", "admin-pass".toCharArray()).username());
    }

    @Test
    void wrongPasswordAndUnknownUserGiveTheSameMessage() {
        createAdmin();
        ValidationException wrongPw = assertThrows(ValidationException.class,
                () -> services.auth().login("admin", "nope-nope".toCharArray()));
        ValidationException noUser = assertThrows(ValidationException.class,
                () -> services.auth().login("ghost", "nope-nope".toCharArray()));
        // Different messages would tell an attacker which usernames exist
        assertEquals(wrongPw.getMessage(), noUser.getMessage());
    }

    @Test
    void emptyFieldsAreRejected() {
        assertThrows(ValidationException.class, () -> services.auth().login("", "x".toCharArray()));
        assertThrows(ValidationException.class, () -> services.auth().login("admin", new char[0]));
    }

    @Test
    void fiveFailuresLockTheAccountEvenForTheRightPassword() {
        createAdmin();
        for (int i = 0; i < 5; i++) {
            assertThrows(ValidationException.class, () -> services.auth().login("admin", "wrong-pass".toCharArray()));
        }
        ValidationException locked = assertThrows(ValidationException.class,
                () -> services.auth().login("admin", "admin-pass".toCharArray()));
        assertTrue(locked.getMessage().startsWith("Too many failed attempts"), locked.getMessage());

        clock.advance(Duration.ofMinutes(1));   // after the lock, the right password works again
        assertEquals("admin", services.auth().login("admin", "admin-pass".toCharArray()).username());
    }

    @Test
    void librarianLogsInWithTheirRole() {
        createAdmin();
        services.users().create("Luis", "Pérez", 25, "luis", "luis-pass".toCharArray(), Role.LIBRARIAN);
        assertEquals(Role.LIBRARIAN, services.auth().login("luis", "luis-pass".toCharArray()).role());
    }
}
