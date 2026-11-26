package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import shelfwise.SampleData;
import shelfwise.Services;
import shelfwise.security.PasswordHasher;

/**
 * First-run wizard: creates the first admin account.
 *
 * v1 shipped a fixed admin username and password inside the code. v2 has
 * no built-in account at all: the first person to open the app chooses
 * the admin credentials, and they are stored as a hash.
 */
public final class SetupDialog extends JDialog {

    private final Services services;
    private final JTextField firstName = new JTextField(18);
    private final JTextField lastName = new JTextField(18);
    private final JSpinner age = new JSpinner(new SpinnerNumberModel(30, 16, 100, 1));
    private final JTextField username = new JTextField("admin", 18);
    private final JPasswordField password = new JPasswordField(18);
    private final JPasswordField repeat = new JPasswordField(18);
    private final JCheckBox sampleData = new JCheckBox("Add sample books and members", true);
    private boolean done;

    private SetupDialog(Services services) {
        super((java.awt.Frame) null, "Shelfwise - First run", ModalityType.APPLICATION_MODAL);
        this.services = services;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel intro = new JLabel("<html><b>Welcome!</b> Create the administrator account.<br>"
                + "You will use it to sign in and to add librarians.</html>");

        FormPanel form = new FormPanel();
        form.addFullRow(intro);
        form.addRow("First name:", firstName);
        form.addRow("Last name:", lastName);
        form.addRow("Age:", age);
        form.addRow("Username:", username);
        form.addRow("Password:", password);
        form.addRow("Repeat password:", repeat);
        form.addFullRow(new JLabel("At least 8 characters."));
        form.addFullRow(sampleData);

        JButton create = new JButton("Create admin");
        JButton cancel = new JButton("Exit");
        create.addActionListener(e -> create());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(cancel);
        buttons.add(create);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        content.add(form, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(create);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Shows the wizard and waits for it.
     *
     * @return true if the admin was created, false if the user chose Exit
     */
    public static boolean showSetup(Services services) {
        SetupDialog dialog = new SetupDialog(services);
        dialog.setVisible(true);   // modal: blocks until disposed
        return dialog.done;
    }

    private void create() {
        char[] a = password.getPassword();
        char[] b = repeat.getPassword();
        try {
            if (!Arrays.equals(a, b)) {
                Dialogs.warn(this, "The two passwords are different.");
                return;
            }
            boolean created = Dialogs.run(this, () -> {
                services.auth().createFirstAdmin(firstName.getText(), lastName.getText(),
                        (Integer) age.getValue(), username.getText(), a);
                if (sampleData.isSelected()) {
                    SampleData.load(services);
                }
            });
            if (created) {
                done = true;
                Dialogs.info(this, "Admin created. Sign in with \"" + username.getText().strip() + "\".");
                dispose();
            }
        } finally {
            PasswordHasher.wipe(a);
            PasswordHasher.wipe(b);
        }
    }
}
