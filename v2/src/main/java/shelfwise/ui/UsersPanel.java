package shelfwise.ui;

import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.security.PasswordHasher;
import shelfwise.service.UserService;
import static shelfwise.ui.ListTableModel.Column;

/**
 * "Staff" tab (admins only): accounts that can log in.
 */
public final class UsersPanel extends CrudPanel<User> {

    private final UserService users;
    private final User currentUser;   // the admin using the app (to protect their own account)

    private final JTextField firstName = new JTextField(16);
    private final JTextField lastName = new JTextField(16);
    // A spinner can't hold an invalid age, unlike v1's 18-40 combo box of strings
    private final JSpinner age = new JSpinner(new SpinnerNumberModel(25, 16, 100, 1));
    private final JTextField username = new JTextField(16);
    private final JComboBox<Role> role = new JComboBox<>(Role.values());
    private final JPasswordField password = new JPasswordField(16);
    private final JLabel passwordNote = new JLabel();
    private final JButton resetPassword = new JButton("Reset password...");

    public UsersPanel(UserService users, User currentUser) {
        super("account", List.of(
                Column.<User>number("ID", User::id),
                Column.<User>text("First name", User::firstName),
                Column.<User>text("Last name", User::lastName),
                Column.<User>number("Age", User::age),
                Column.<User>text("Username", User::username),
                Column.<User>text("Role", User::role)));
        this.users = users;
        this.currentUser = currentUser;
        resetPassword.addActionListener(e -> resetSelectedPassword());
        initUi();
    }

    @Override
    protected List<User> load(String search) {
        // There are only a few staff accounts, so filtering in memory is fine
        String text = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
        return users.list().stream()
                .filter(u -> text.isEmpty()
                        || u.fullName().toLowerCase(Locale.ROOT).contains(text)
                        || u.username().toLowerCase(Locale.ROOT).contains(text))
                .toList();
    }

    @Override
    protected JComponent buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("First name:", firstName);
        form.addRow("Last name:", lastName);
        form.addRow("Age:", age);
        form.addRow("Username:", username);
        form.addRow("Role:", role);
        form.addRow("Password:", password);
        form.addFullRow(passwordNote);
        form.addFiller();
        return form;
    }

    @Override
    protected List<JComponent> extraButtons() {
        return List.of(resetPassword);
    }

    @Override
    protected void onEditingChanged(User current) {
        // The password field is only for new accounts; existing ones use "Reset password..."
        boolean isNew = current == null;
        password.setEnabled(isNew);
        resetPassword.setEnabled(!isNew);
        passwordNote.setText(isNew
                ? "At least 8 characters. It is stored as a hash."
                : "To change it, use \"Reset password...\".");
    }

    @Override
    protected void fillForm(User user) {
        firstName.setText(user.firstName());
        lastName.setText(user.lastName());
        age.setValue(user.age());
        username.setText(user.username());
        role.setSelectedItem(user.role());
        password.setText("");
    }

    @Override
    protected void clearForm() {
        firstName.setText("");
        lastName.setText("");
        age.setValue(25);
        username.setText("");
        role.setSelectedItem(Role.LIBRARIAN);
        password.setText("");
        firstName.requestFocusInWindow();
    }

    @Override
    protected User save(User current) {
        Role chosen = (Role) role.getSelectedItem();
        int years = (Integer) age.getValue();
        if (current == null) {
            char[] pw = password.getPassword();
            try {
                return users.create(firstName.getText(), lastName.getText(), years, username.getText(), pw, chosen);
            } finally {
                PasswordHasher.wipe(pw);   // don't leave the password in memory
                password.setText("");
            }
        }
        return users.update(current.id(), firstName.getText(), lastName.getText(), years, username.getText(), chosen, currentUser);
    }

    @Override
    protected void delete(User user) {
        users.delete(user.id(), currentUser);
    }

    @Override
    protected long idOf(User user) {
        return user.id();
    }

    @Override
    protected String describe(User user) {
        return user.username();
    }

    @Override
    protected String searchHint() {
        return "Name or username";
    }

    private void resetSelectedPassword() {
        User target = editing();
        if (target == null) {
            return;
        }
        JPasswordField first = new JPasswordField(16);
        JPasswordField second = new JPasswordField(16);
        FormPanel form = new FormPanel();
        form.addRow("New password:", first);
        form.addRow("Repeat it:", second);
        int answer = JOptionPane.showConfirmDialog(this, form, "Reset password of " + target.username(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        char[] a = first.getPassword();
        char[] b = second.getPassword();
        try {
            if (!java.util.Arrays.equals(a, b)) {
                Dialogs.warn(this, "The two passwords are different.");
                return;
            }
            if (Dialogs.run(this, () -> users.resetPassword(target.id(), a))) {
                Dialogs.info(this, "Password of " + target.username() + " changed.");
            }
        } finally {
            PasswordHasher.wipe(a);
            PasswordHasher.wipe(b);
        }
    }
}
