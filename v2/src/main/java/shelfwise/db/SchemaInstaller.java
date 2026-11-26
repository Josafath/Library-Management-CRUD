package shelfwise.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import shelfwise.repository.DataAccessException;

/**
 * Creates the tables when the app starts, so there is no SQL script to run
 * by hand. Every statement in schema.sql uses "IF NOT EXISTS", so running
 * it on an existing database changes nothing.
 */
public final class SchemaInstaller {

    /** Location of the script inside the jar (src/main/resources). */
    static final String SCHEMA_RESOURCE = "/shelfwise/schema.sql";

    private SchemaInstaller() {
        // Utility class: no instances
    }

    /** Runs schema.sql against the database. */
    public static void install(ConnectionFactory connections) {
        List<String> statements = splitStatements(readSchema());
        try (Connection conn = connections.open();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create the database tables", ex);
        }
    }

    /**
     * Splits a SQL script into single statements: drops "--" comment lines
     * and cuts at every ";". (The script has no ";" inside strings, so this
     * simple rule is enough.)
     */
    static List<String> splitStatements(String script) {
        StringBuilder withoutComments = new StringBuilder();
        for (String line : script.split("\\R")) {          // \R = any line break (\n or \r\n)
            if (!line.strip().startsWith("--")) {
                withoutComments.append(line).append('\n');
            }
        }
        List<String> statements = new ArrayList<>();
        for (String part : withoutComments.toString().split(";")) {
            if (!part.isBlank()) {
                statements.add(part.strip());
            }
        }
        return statements;
    }

    private static String readSchema() {
        try (InputStream in = SchemaInstaller.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(SCHEMA_RESOURCE + " is missing from the build");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
