package shelfwise.service;

import java.util.List;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.repository.UserRepository;
import shelfwise.security.PasswordHasher;

/**
 * Staff accounts: create, edit, delete and passwords.
 */
public final class UserService {

    private final UserRepository users;
    private final PasswordHasher hasher;

    public UserService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public List<User> list() {
        return users.findAll();
    }

    /** Creates an account; the password is stored only as a hash. */
    public User create(String firstName, String lastName, int age, String username, char[] password, Role role) {
        User draft = validated(0, firstName, lastName, age, username, role, "");
        Validation.requirePassword(password);
        try {
            return users.insert(draft.withPasswordHash(hasher.hash(password)));
        } catch (DuplicateKeyException ex) {
            throw new ValidationException("The username \"" + draft.username() + "\" is already taken.");
        }
    }

    /**
     * Edits name, age, username and role (the "U" that v1 was missing).
     *
     * @param currentUser who is making the change (to protect the last admin)
     */
    public User update(long id, String firstName, String lastName, int age, String username, Role role, User currentUser) {
        User existing = require(id);
        User changed = validated(id, firstName, lastName, age, username, role, existing.passwordHash());

        // Demoting an admin must leave at least one admin, or nobody could manage accounts
        if (existing.role() == Role.ADMIN && role != Role.ADMIN) {
            if (id == currentUser.id()) {
                throw new ValidationException("You can't remove your own admin role.");
            }
            if (users.countByRole(Role.ADMIN) <= 1) {
                throw new ValidationException("There must be at least one admin.");
            }
        }
        try {
            users.update(changed);
            return changed;
        } catch (DuplicateKeyException ex) {
            throw new ValidationException("The username \"" + changed.username() + "\" is already taken.");
        }
    }

    /** An admin sets a new password for another account. */
    public void resetPassword(long id, char[] newPassword) {
        require(id);
        Validation.requirePassword(newPassword);
        users.updatePasswordHash(id, hasher.hash(newPassword));
    }

    /** A user changes their own password; they must know the current one. */
    public void changeOwnPassword(User currentUser, char[] currentPassword, char[] newPassword) {
        User fresh = require(currentUser.id());   // read the hash again: it may have been reset
        if (!hasher.verify(currentPassword, fresh.passwordHash())) {
            throw new ValidationException("The current password is wrong.");
        }
        Validation.requirePassword(newPassword);
        users.updatePasswordHash(fresh.id(), hasher.hash(newPassword));
    }

    /**
     * Deletes an account. Nobody can delete themselves, and the last admin
     * can't be deleted (v1 let the admin lock everyone out).
     */
    public void delete(long id, User currentUser) {
        User target = require(id);
        if (target.id() == currentUser.id()) {
            throw new ValidationException("You can't delete your own account.");
        }
        if (target.role() == Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ValidationException("There must be at least one admin.");
        }
        users.delete(id);
    }

    private User require(long id) {
        return users.findById(id)
                .orElseThrow(() -> new ValidationException("That account no longer exists. Refresh the list."));
    }

    private static User validated(long id, String firstName, String lastName, int age,
                                  String username, Role role, String passwordHash) {
        if (role == null) {
            throw new ValidationException("Choose a role.");
        }
        return new User(
                id,
                Validation.requireText(firstName, "First name", 60),
                Validation.requireText(lastName, "Last name", 60),
                Validation.requireRange(age, "Age", 16, 100),
                Validation.requireUsername(username),
                passwordHash,
                role);
    }
}
