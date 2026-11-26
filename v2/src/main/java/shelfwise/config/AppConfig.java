package shelfwise.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Database settings read from db.properties.
 *
 * @param url      JDBC url, e.g. jdbc:mysql://localhost:3306/shelfwise
 * @param user     MySQL user
 * @param password MySQL password
 */
public record AppConfig(String url, String user, String password) {

    /** File looked up in the folder the app is started from. */
    public static final String DEFAULT_FILE = "db.properties";

    /** System property to use another file: java -Dshelfwise.config=/path/db.properties ... */
    public static final String PATH_PROPERTY = "shelfwise.config";

    /**
     * createDatabaseIfNotExist lets the app create the "shelfwise" database on
     * first run, so the user doesn't have to run any SQL script by hand.
     */
    public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/shelfwise?createDatabaseIfNotExist=true";

    /** Reads the file named by -Dshelfwise.config, or ./db.properties. */
    public static AppConfig load() throws IOException {
        String custom = System.getProperty(PATH_PROPERTY);
        return load(Path.of(custom != null && !custom.isBlank() ? custom : DEFAULT_FILE));
    }

    /** Reads the given properties file. */
    public static AppConfig load(Path file) throws IOException {
        Properties props = new Properties();
        // Read as UTF-8 (the v1 bug: Properties.load(InputStream) assumes
        // ISO-8859-1 and turns "ñ" in a password into "Ã±")
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(in);
        } catch (NoSuchFileException ex) {
            throw new IOException("Configuration file not found: " + file.toAbsolutePath()
                    + "\nCopy db.properties.example to db.properties and fill in your MySQL user.", ex);
        }

        String user = props.getProperty("db.user", "").trim();
        if (user.isEmpty()) {
            throw new IOException("db.user is missing in " + file.toAbsolutePath());
        }
        // The password is not trimmed: spaces could be part of it
        return new AppConfig(
                props.getProperty("db.url", DEFAULT_URL).trim(),
                user,
                props.getProperty("db.password", ""));
    }

    // Records print every field by default; keep the password out of logs
    @Override
    public String toString() {
        return "AppConfig[url=" + url + ", user=" + user + "]";
    }
}
