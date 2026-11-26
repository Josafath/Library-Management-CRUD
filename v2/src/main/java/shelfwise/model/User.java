package shelfwise.model;

/**
 * A staff account (admin or librarian).
 *
 * @param id           database id; 0 means "not saved yet"
 * @param firstName    first name(s)
 * @param lastName     last name(s)
 * @param age          age in years
 * @param username     unique login name
 * @param passwordHash PBKDF2 hash, never the plain password (see PasswordHasher)
 * @param role         what the account can do
 */
public record User(long id, String firstName, String lastName, int age,
                   String username, String passwordHash, Role role) {

    /** Name shown in the UI, e.g. "Ana López". */
    public String fullName() {
        return firstName + " " + lastName;
    }

    /** Copy of this user with a database id (used after an insert). */
    public User withId(long newId) {
        return new User(newId, firstName, lastName, age, username, passwordHash, role);
    }

    /** Copy of this user with a new password hash. */
    public User withPasswordHash(String newHash) {
        return new User(id, firstName, lastName, age, username, newHash, role);
    }

    // Records print every field by default; keep the hash out of logs
    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", role=" + role.name() + "]";
    }
}
