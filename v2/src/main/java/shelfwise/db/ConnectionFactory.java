package shelfwise.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import shelfwise.config.AppConfig;

/**
 * Opens database connections.
 *
 * v1 opened one connection at login and shared it between windows (and
 * never closed the old one on logout). Here every operation asks for a
 * fresh connection and closes it with try-with-resources, so nothing leaks
 * and a dropped connection doesn't break the whole session.
 */
@FunctionalInterface
public interface ConnectionFactory {

    /** Returns a new connection; the caller must close it. */
    Connection open() throws SQLException;

    /** Factory that uses the JDBC driver with the given settings. */
    static ConnectionFactory fromConfig(AppConfig config) {
        // JDBC 4 drivers register themselves, so no Class.forName(...) is needed
        return () -> DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
