package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import shelfwise.model.Book;
import shelfwise.model.Loan;
import shelfwise.model.Member;
import shelfwise.service.LoanService;

/**
 * Modal dialog to lend a book: choose the book, the member and how many days.
 */
final class NewLoanDialog extends JDialog {

    private final JComboBox<Book> book;
    private final JComboBox<Member> member;
    private final JSpinner days = new JSpinner(new SpinnerNumberModel(LoanService.DEFAULT_DAYS, 1, LoanService.MAX_DAYS, 1));
    private final JLabel dueDate = new JLabel();
    private final LoanService loans;
    private Loan created;   // stays null if the user cancels

    private NewLoanDialog(Window owner, LoanService loans, List<Book> books, List<Member> members) {
        super(owner, "New loan", ModalityType.APPLICATION_MODAL);
        this.loans = loans;
        // Only books nobody has right now are offered
        this.book = new JComboBox<>(books.toArray(Book[]::new));
        this.member = new JComboBox<>(members.toArray(Member[]::new));

        FormPanel form = new FormPanel();
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        form.addRow("Book:", book);
        form.addRow("Member:", member);
        form.addRow("Days:", days);
        form.addRow("Due date:", dueDate);
        days.addChangeListener(e -> updateDueDate());
        updateDueDate();

        JButton lend = new JButton("Lend");
        JButton cancel = new JButton("Cancel");
        lend.addActionListener(e -> lend());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(cancel);
        buttons.add(lend);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(lend);   // Enter = Lend
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog.
     *
     * @return the new loan, or null if it was cancelled or nothing can be lent
     */
    static Loan show(Window owner, LoanService loans, List<Book> availableBooks, List<Member> members) {
        if (availableBooks.isEmpty()) {
            Dialogs.info(owner, "There are no available books to lend.");
            return null;
        }
        if (members.isEmpty()) {
            Dialogs.info(owner, "Add a member in the Members tab first.");
            return null;
        }
        NewLoanDialog dialog = new NewLoanDialog(owner, loans, availableBooks, members);
        dialog.setVisible(true);   // blocks here until the dialog closes
        return dialog.created;
    }

    private void updateDueDate() {
        int n = (Integer) days.getValue();
        // ISO format (2026-12-09), the same the Loans table uses
        dueDate.setText(loans.today().plusDays(n).toString());
    }

    private void lend() {
        Book b = (Book) book.getSelectedItem();
        Member m = (Member) member.getSelectedItem();
        if (b == null || m == null) {
            return;
        }
        if (Dialogs.run(this, () -> created = loans.checkout(b.id(), m.id(), (Integer) days.getValue()))) {
            dispose();
        }
    }
}
