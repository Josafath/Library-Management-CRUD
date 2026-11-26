package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import shelfwise.Services;
import shelfwise.model.User;
import shelfwise.security.PasswordHasher;
import shelfwise.service.ValidationException;

/**
 * Login window. One login screen for everybody: the role stored with the
 * account decides which tabs the main window shows.
 */
public final class LoginFrame extends JFrame {

    private final Services services;
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JLabel message = new JLabel(" ");

    public LoginFrame(Services services) {
        super("Shelfwise - Sign in");
        this.services = services;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JLabel title = new JLabel("Shelfwise", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Library management", SwingConstants.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        FormPanel form = new FormPanel();
        form.addRow("Username:", username);
        form.addRow("Password:", password);
        message.setForeground(new Color(176, 0, 32));   // dark red, readable on light backgrounds
        form.addFullRow(message);

        JButton signIn = new JButton("Sign in");
        signIn.addActionListener(e -> signIn());
        form.addFullRow(signIn);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        content.add(header, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        setContentPane(content);

        getRootPane().setDefaultButton(signIn);   // Enter signs in
        pack();
        setResizable(false);
        setLocationRelativeTo(null);              // center on screen
    }

    private void signIn() {
        char[] pw = password.getPassword();
        AtomicReference<User> user = new AtomicReference<>();
        try {
            // Wrong credentials are shown in the red label instead of a popup
            Dialogs.run(this, () -> {
                try {
                    user.set(services.auth().login(username.getText(), pw));
                } catch (ValidationException ex) {
                    message.setText(ex.getMessage());
                }
            });
        } finally {
            PasswordHasher.wipe(pw);   // don't leave the password in memory
            password.setText("");
        }
        if (user.get() != null) {
            dispose();
            new MainFrame(services, user.get()).setVisible(true);
        } else {
            password.requestFocusInWindow();
        }
    }
}
