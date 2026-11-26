package shelfwise.repository;

/**
 * A database problem (connection lost, bad SQL, ...).
 *
 * It is unchecked so the service and UI layers don't need to know about
 * java.sql; the UI shows its message in an error dialog.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
