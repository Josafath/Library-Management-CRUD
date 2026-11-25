package libraryproject_;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens the MySQL connection using the settings stored in db.properties.
 *
 * The credentials used to be hardcoded in Principal.java (and pushed to
 * GitHub). Now they live in a local file that git ignores; the repo only
 * ships db.properties.example as a template.
 *
 * @author kevin
 */
public final class Database {

    // File read from the folder the app is started in (NetBeans uses the project folder)
    private static final String CONFIG_FILE = "db.properties";

    private Database() {
        // Utility class: no instances
    }

    /**
     * Reads db.properties and returns a new connection.
     *
     * @throws IOException  if the config file is missing or unreadable
     * @throws SQLException if MySQL rejects the connection
     */
    public static Connection connect() throws IOException, SQLException {
        Properties config = new Properties();
        // Read the file as UTF-8: load(InputStream) assumes ISO-8859-1 and
        // would corrupt passwords with characters like ñ or accents
        try (Reader in = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            config.load(in);
        } catch (IOException ex) {
            throw new IOException("Could not read " + CONFIG_FILE
                    + ". Copy db.properties.example to db.properties and fill in your MySQL user.", ex);
        }

        // Defaults match the schema.sql script, so only the user/password are required
        String url = config.getProperty("db.url", "jdbc:mysql://localhost:3306/libreria");
        String user = config.getProperty("db.user", "root");
        String password = config.getProperty("db.password", "");

        return DriverManager.getConnection(url, user, password);
    }
}
