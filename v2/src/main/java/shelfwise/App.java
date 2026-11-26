package shelfwise;

import java.io.IOException;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import shelfwise.config.AppConfig;
import shelfwise.db.ConnectionFactory;
import shelfwise.db.SchemaInstaller;
import shelfwise.repository.DataAccessException;
import shelfwise.ui.Dialogs;
import shelfwise.ui.LoginFrame;
import shelfwise.ui.SetupDialog;

/**
 * Entry point. Start-up steps:
 *
 * 1. read db.properties
 * 2. connect and create the tables if they don't exist
 * 3. first run only: create the admin account
 * 4. show the login window
 */
public final class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private App() {
        // Not instantiable: only main()
    }

    public static void main(String[] args) {
        // Every Swing window must be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        useNimbusLookAndFeel();

        // 1. Settings
        AppConfig config;
        try {
            config = AppConfig.load();
        } catch (IOException ex) {
            Dialogs.error(null, "Shelfwise - configuration", ex.getMessage());
            return;
        }

        // 2. Database (creates the "shelfwise" database and tables on first run)
        ConnectionFactory connections = ConnectionFactory.fromConfig(config);
        try {
            SchemaInstaller.install(connections);
        } catch (DataAccessException ex) {
            LOG.log(Level.SEVERE, "Could not prepare the database", ex);
            Dialogs.error(null, "Shelfwise - database",
                    "Could not connect to MySQL at " + config.url() + "\n\n" + rootMessage(ex)
                    + "\n\nCheck that MySQL is running and the user/password in db.properties.");
            return;
        }

        Services services = Services.mysql(connections, Clock.systemDefaultZone());

        // 3. First run: no admin yet
        try {
            if (services.auth().needsSetup() && !SetupDialog.showSetup(services)) {
                return;   // the user chose "Exit" in the wizard
            }
        } catch (DataAccessException ex) {
            Dialogs.error(null, "Shelfwise - database", rootMessage(ex));
            return;
        }

        // 4. Login
        new LoginFrame(services).setVisible(true);
    }

    // The innermost cause has the useful text, e.g. "Access denied for user 'root'@'localhost'"
    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }

    // Nimbus ships with the JDK and looks the same on Windows, macOS and Linux
    private static void useNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            LOG.log(Level.INFO, "Nimbus not available, using the default look and feel", ex);
        }
    }
}
