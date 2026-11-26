package shelfwise.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelfwise.Services;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class UserServiceTest {

    private InMemoryRepositories db;
    private Services services;
    private UserService users;
    private User admin;

    @BeforeEach
    void setUp() {
        db = new InMemoryRepositories();
        services = db.services(new MutableClock(LocalDate.of(2026, 3, 1)));
        users = services.users();
        admin = services.auth().createFirstAdmin("Ana", "López", 30, "admin", "admin-pass".toCharArray());
    }

    private User librarian(String username) {
        return users.create("Luis", "Pérez", 25, username, "librarian-pass".toCharArray(), Role.LIBRARIAN);
    }

    private static String message(Runnable action) {
        return assertThrows(ValidationException.class, action::run).getMessage();
    }

    @Test
    void createTrimsTheFields() {
        User u = users.create("  Luis ", " Pérez ", 25, " luis ", "librarian-pass".toCharArray(), Role.LIBRARIAN);
        assertEquals("Luis", u.firstName());
        assertEquals("Pérez", u.lastName());
        assertEquals("luis", u.username());
    }

    @Test
    void duplicateUsernameGivesAFriendlyMessage() {
        librarian("luis");
        assertEquals("The username \"LUIS\" is already taken.", message(() -> librarian("LUIS")));
    }

    @Test
    void validatesTheForm() {
        char[] ok = "long-enough".toCharArray();
        assertEquals("First name is required.", message(() -> users.create(" ", "P", 25, "luis", ok, Role.LIBRARIAN)));
        assertEquals("Age must be between 16 and 100.", message(() -> users.create("L", "P", 12, "luis", ok, Role.LIBRARIAN)));
        assertTrue(message(() -> users.create("L", "P", 25, "a b", ok, Role.LIBRARIAN)).startsWith("Username must be"));
        assertTrue(message(() -> users.create("L", "P", 25, "luis", "short".toCharArray(), Role.LIBRARIAN)).startsWith("Password must have"));
        assertEquals("Choose a role.", message(() -> users.create("L", "P", 25, "luis", ok, null)));
    }

    @Test
    void updateChangesDataButKeepsThePassword() {
        User luis = librarian("luis");
        users.update(luis.id(), "Luis Alberto", "Pérez", 26, "luisa", Role.LIBRARIAN, admin);

        User saved = db.users.findById(luis.id()).orElseThrow();
        assertEquals("Luis Alberto", saved.firstName());
        assertEquals(26, saved.age());
        assertEquals("luisa", saved.username());
        // The old password still works under the new username
        assertEquals(luis.id(), services.auth().login("luisa", "librarian-pass".toCharArray()).id());
    }

    @Test
    void updateToATakenUsernameIsRejected() {
        User luis = librarian("luis");
        librarian("carla");
        assertTrue(message(() -> users.update(luis.id(), "L", "P", 25, "carla", Role.LIBRARIAN, admin)).contains("already taken"));
    }

    @Test
    void adminCannotRemoveTheirOwnAdminRole() {
        assertEquals("You can't remove your own admin role.",
                message(() -> users.update(admin.id(), "Ana", "López", 30, "admin", Role.LIBRARIAN, admin)));
    }

    @Test
    void anotherAdminCanBeDemotedWhileOneRemains() {
        User second = users.create("Eva", "Ruiz", 40, "eva", "second-admin".toCharArray(), Role.ADMIN);
        assertDoesNotThrow(() -> users.update(second.id(), "Eva", "Ruiz", 40, "eva", Role.LIBRARIAN, admin));
        assertEquals(1, db.users.countByRole(Role.ADMIN));
    }

    @Test
    void nobodyCanDeleteThemselves() {
        assertEquals("You can't delete your own account.", message(() -> users.delete(admin.id(), admin)));
    }

    @Test
    void theLastAdminCannotBeDeleted() {
        // With two admins, one can be deleted...
        User second = users.create("Eva", "Ruiz", 40, "eva", "second-admin".toCharArray(), Role.ADMIN);
        users.delete(second.id(), admin);
        // ...but not the last one. (Librarians never see the Staff tab; the
        // service enforces the rule anyway, whoever calls it.)
        User luis = librarian("luis");
        assertEquals("There must be at least one admin.", message(() -> users.delete(admin.id(), luis)));
    }

    @Test
    void librariansCanBeDeleted() {
        User luis = librarian("luis");
        users.delete(luis.id(), admin);
        assertTrue(db.users.findById(luis.id()).isEmpty());
    }

    @Test
    void resetPasswordReplacesTheHash() {
        User luis = librarian("luis");
        users.resetPassword(luis.id(), "brand-new-pass".toCharArray());
        assertEquals(luis.id(), services.auth().login("luis", "brand-new-pass".toCharArray()).id());
        assertThrows(ValidationException.class, () -> services.auth().login("luis", "librarian-pass".toCharArray()));
    }

    @Test
    void changeOwnPasswordNeedsTheCurrentOne() {
        assertEquals("The current password is wrong.",
                message(() -> users.changeOwnPassword(admin, "guess-guess".toCharArray(), "new-password".toCharArray())));

        users.changeOwnPassword(admin, "admin-pass".toCharArray(), "new-password".toCharArray());
        assertEquals(admin.id(), services.auth().login("admin", "new-password".toCharArray()).id());
    }

    @Test
    void actionsOnDeletedAccountsExplainWhatHappened() {
        assertTrue(message(() -> users.resetPassword(999, "whatever-pass".toCharArray())).contains("no longer exists"));
    }
}
