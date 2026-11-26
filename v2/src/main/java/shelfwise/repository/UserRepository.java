package shelfwise.repository;

import java.util.List;
import java.util.Optional;
import shelfwise.model.Role;
import shelfwise.model.User;

/**
 * Storage of staff accounts. The services only talk to this interface, so
 * tests can use an in-memory version instead of MySQL.
 */
public interface UserRepository {

    /** Every account, ordered by id. */
    List<User> findAll();

    Optional<User> findById(long id);

    /** Looks up an account by login name (case-insensitive, like MySQL's default collation). */
    Optional<User> findByUsername(String username);

    /** How many accounts have the given role. */
    int countByRole(Role role);

    /**
     * Saves a new account.
     *
     * @return the same user with its new id
     * @throws DuplicateKeyException if the username is taken
     */
    User insert(User user);

    /**
     * Saves name, age, username and role. The password is changed with
     * {@link #updatePasswordHash} only.
     *
     * @throws DuplicateKeyException if the new username is taken
     */
    void update(User user);

    void updatePasswordHash(long id, String passwordHash);

    void delete(long id);
}
