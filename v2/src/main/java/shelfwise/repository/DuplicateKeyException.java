package shelfwise.repository;

/**
 * An insert or update broke a UNIQUE rule (same username, same email, or a
 * book that is already on loan).
 */
public class DuplicateKeyException extends DataAccessException {

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
