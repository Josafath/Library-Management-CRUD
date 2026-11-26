package shelfwise.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import shelfwise.repository.DataAccessException;
import shelfwise.repository.DuplicateKeyException;

/**
 * Small helpers shared by the JDBC repositories.
 */
final class JdbcSupport {

    /** MySQL error code for "Duplicate entry ... for key ...". */
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private JdbcSupport() {
        // Utility class: no instances
    }

    /**
     * Wraps a SQLException in our own unchecked exceptions, so the layers
     * above don't depend on java.sql. Duplicate keys get their own type
     * because the services turn them into friendly messages.
     */
    static DataAccessException translate(String action, SQLException ex) {
        if (ex.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
            return new DuplicateKeyException(action + ": duplicate value", ex);
        }
        return new DataAccessException(action + ": " + ex.getMessage(), ex);
    }

    /** Reads the AUTO_INCREMENT id MySQL generated for the last insert. */
    static long generatedId(PreparedStatement stmt) throws SQLException {
        try (ResultSet keys = stmt.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("The database did not return the new id");
            }
            return keys.getLong(1);
        }
    }

    /** Flag for prepareStatement(...) so getGeneratedKeys() works. */
    static final int RETURN_KEYS = Statement.RETURN_GENERATED_KEYS;

    /**
     * Turns user text into a LIKE pattern: "%text%", with the LIKE wildcards
     * % and _ escaped so they are searched literally. Used with
     * "LIKE ? ESCAPE '!'".
     */
    static String likePattern(String text) {
        String safe = text.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + safe + "%";
    }
}
