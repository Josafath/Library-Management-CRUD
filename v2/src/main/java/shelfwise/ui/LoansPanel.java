package shelfwise.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import shelfwise.model.Loan;
import shelfwise.service.BookService;
import shelfwise.service.LoanService;
import shelfwise.service.MemberService;
import static shelfwise.ui.ListTableModel.Column;

/**
 * "Loans" tab (new in v2): lend books, register returns, see what is overdue.
 */
public final class LoansPanel extends JPanel implements Refreshable {

    private final LoanService loans;
    private final BookService books;
    private final MemberService members;

    private final JComboBox<LoanService.Filter> filter = new JComboBox<>(LoanService.Filter.values());
    private final ListTableModel<Loan> model;
    private final JTable table;
    private final JLabel status = new JLabel(" ");
    private final JButton returnButton = new JButton("Register return");

    // Soft red for overdue rows, readable with any look and feel
    private static final Color OVERDUE = new Color(255, 214, 214);

    public LoansPanel(LoanService loans, BookService books, MemberService members) {
        super(new BorderLayout(8, 8));
        this.loans = loans;
        this.books = books;
        this.members = members;

        model = new ListTableModel<>(List.of(
                Column.<Loan>number("ID", Loan::id),
                Column.<Loan>text("Book", Loan::bookTitle),
                Column.<Loan>text("Member", Loan::memberName),
                // ISO dates (2026-11-25) sort correctly as plain text
                Column.<Loan>text("Lent", l -> l.loanDate().toString()),
                Column.<Loan>text("Due", l -> l.dueDate().toString()),
                Column.<Loan>text("Returned", l -> l.returnDate() == null ? "" : l.returnDate().toString()),
                Column.<Loan>text("Status", this::statusOf)));

        // Overrides prepareRenderer to paint overdue rows red
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Loan loan = model.getItem(convertRowIndexToModel(row));
                    c.setBackground(loan.isOverdue(loans.today()) ? OVERDUE : UIManager.getColor("Table.background"));
                }
                return c;
            }
        };
        Tables.setUp(table);
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        top.add(new JLabel("Show:"));
        top.add(filter);
        filter.addActionListener(e -> refresh());
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton lend = new JButton("New loan...");
        lend.addActionListener(e -> newLoan());
        returnButton.addActionListener(e -> registerReturn());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(lend);
        buttons.add(returnButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(status, BorderLayout.WEST);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        updateButtons();
    }

    @Override
    public void refresh() {
        Dialogs.run(this, () -> {
            List<Loan> list = loans.list((LoanService.Filter) filter.getSelectedItem());
            model.setItems(list);
            long overdue = list.stream().filter(l -> l.isOverdue(loans.today())).count();
            status.setText(list.size() + " loan(s)" + (overdue > 0 ? ", " + overdue + " overdue" : ""));
        });
        updateButtons();
    }

    // "On loan", "Overdue (3 days)" or "Returned"
    private String statusOf(Loan loan) {
        if (!loan.isActive()) {
            return "Returned";
        }
        LocalDate today = loans.today();
        if (loan.isOverdue(today)) {
            long late = ChronoUnit.DAYS.between(loan.dueDate(), today);
            return "Overdue (" + late + (late == 1 ? " day)" : " days)");
        }
        return "On loan";
    }

    private Loan selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.getItem(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        Loan loan = selected();
        returnButton.setEnabled(loan != null && loan.isActive());
    }

    private void newLoan() {
        Dialogs.run(this, () -> {
            Loan created = NewLoanDialog.show(SwingUtilities.getWindowAncestor(this), loans,
                    books.available(), members.search(""));
            if (created != null) {
                refresh();
                status.setText("\"" + created.bookTitle() + "\" lent to " + created.memberName()
                        + " until " + created.dueDate() + ".");
            }
        });
    }

    private void registerReturn() {
        Loan loan = selected();
        if (loan == null || !Dialogs.confirm(this, "Register the return of \"" + loan.bookTitle() + "\"?")) {
            return;
        }
        if (Dialogs.run(this, () -> loans.returnBook(loan.id()))) {
            refresh();
            status.setText("\"" + loan.bookTitle() + "\" returned.");
        }
    }
}
