package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import shelfwise.Services;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.security.PasswordHasher;

/**
 * Main window after login. v1 had two separate windows (admin and
 * librarian); here there is one window and the tabs depend on the role:
 *
 * - Librarian: Books, Members, Loans
 * - Admin:     the same, plus Staff
 */
public final class MainFrame extends JFrame {

    private final Services services;
    private final User user;

    public MainFrame(Services services, User user) {
        super("Shelfwise");
        this.services = services;
        this.user = user;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // --- Header: app name, who is signed in, account buttons ---
        JLabel appName = new JLabel("Shelfwise");
        appName.setFont(appName.getFont().deriveFont(Font.BOLD, 20f));
        JLabel who = new JLabel("Signed in as " + user.fullName() + " (" + user.role() + ")");

        JButton changePassword = new JButton("Change password...");
        JButton logout = new JButton("Log out");
        changePassword.addActionListener(e -> changePassword());
        logout.addActionListener(e -> logout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
        left.add(appName);
        left.add(who);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
        right.add(changePassword);
        right.add(logout);
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        // --- Tabs ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", new BooksPanel(services.books()));
        tabs.addTab("Members", new MembersPanel(services.members()));
        tabs.addTab("Loans", new LoansPanel(services.loans(), services.books(), services.members()));
        if (user.role() == Role.ADMIN) {
            tabs.addTab("Staff", new UsersPanel(services.users(), user));
        }
        // Reload a tab every time it is shown, so changes made in other tabs appear
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() instanceof Refreshable tab) {
                tab.refresh();
            }
        });
        ((Refreshable) tabs.getComponentAt(0)).refresh();   // first tab on start

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setMinimumSize(new Dimension(900, 520));
        setSize(1100, 640);
        setLocationRelativeTo(null);
    }

    private void logout() {
        dispose();   // nothing else to close: every query opens and closes its own connection
        new LoginFrame(services).setVisible(true);
    }

    private void changePassword() {
        JPasswordField current = new JPasswordField(16);
        JPasswordField first = new JPasswordField(16);
        JPasswordField second = new JPasswordField(16);
        FormPanel form = new FormPanel();
        form.addRow("Current password:", current);
        form.addRow("New password:", first);
        form.addRow("Repeat new password:", second);
        int answer = JOptionPane.showConfirmDialog(this, form, "Change password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        char[] now = current.getPassword();
        char[] a = first.getPassword();
        char[] b = second.getPassword();
        try {
            if (!Arrays.equals(a, b)) {
                Dialogs.warn(this, "The two new passwords are different.");
                return;
            }
            if (Dialogs.run(this, () -> services.users().changeOwnPassword(user, now, a))) {
                Dialogs.info(this, "Password changed.");
            }
        } finally {
            PasswordHasher.wipe(now);
            PasswordHasher.wipe(a);
            PasswordHasher.wipe(b);
        }
    }
}
