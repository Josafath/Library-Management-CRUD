package shelfwise.service;

import java.time.Duration;
import java.util.Optional;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.repository.UserRepository;
import shelfwise.security.LoginThrottle;
import shelfwise.security.PasswordHasher;

/**
 * Login and first-run setup.
 */
public final class AuthService {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final LoginThrottle throttle;

    // Hash of a random password, checked when the username doesn't exist (see login)
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordHasher hasher, LoginThrottle throttle) {
        this.users = users;
        this.hasher = hasher;
        this.throttle = throttle;
        this.dummyHash = hasher.hash("not-a-real-password".toCharArray());
    }

    /**
     * Checks the credentials.
     *
     * @return the logged-in user
     * @throws ValidationException with a message for the login screen
     */
    public User login(String username, char[] password) {
        String name = username == null ? "" : username.strip();
        if (name.isEmpty() || password == null || password.length == 0) {
            throw new ValidationException("Enter your username and password.");
        }

        Duration locked = throttle.remainingLock(name);
        if (!locked.isZero()) {
            long seconds = Math.max(1, locked.toSeconds());
            throw new ValidationException("Too many failed attempts. Try again in " + seconds + " seconds.");
        }

        Optional<User> found = users.findByUsername(name);
        // When the user doesn't exist we still run a hash check. Otherwise
        // "wrong username" would answer much faster than "wrong password",
        // and the response time would reveal which usernames exist.
        boolean ok = hasher.verify(password, found.map(User::passwordHash).orElse(dummyHash))
                && found.isPresent();

        if (!ok) {
            throttle.recordFailure(name);
            // Same message for both cases, for the same reason
            throw new ValidationException("Wrong username or password.");
        }
        throttle.recordSuccess(name);
        return found.get();
    }

    /** True when there is no admin yet (first run): the app shows the setup dialog. */
    public boolean needsSetup() {
        return users.countByRole(Role.ADMIN) == 0;
    }

    /**
     * Creates the first admin account. Only allowed while there is no admin,
     * so the setup dialog can't be abused later to create one.
     */
    public User createFirstAdmin(String firstName, String lastName, int age, String username, char[] password) {
        if (!needsSetup()) {
            throw new ValidationException("An administrator already exists.");
        }
        return new UserService(users, hasher).create(firstName, lastName, age, username, password, Role.ADMIN);
    }
}
