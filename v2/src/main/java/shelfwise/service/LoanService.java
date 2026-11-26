package shelfwise.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import shelfwise.model.Book;
import shelfwise.model.Loan;
import shelfwise.model.Member;
import shelfwise.repository.BookRepository;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.repository.LoanRepository;
import shelfwise.repository.MemberRepository;

/**
 * Lending books and getting them back.
 */
public final class LoanService {

    /** Default loan length in days. */
    public static final int DEFAULT_DAYS = 14;
    /** Longest loan allowed, in days. */
    public static final int MAX_DAYS = 60;
    /** How many books one member can have at the same time. */
    public static final int MAX_ACTIVE_PER_MEMBER = 3;

    /** Which loans to list. */
    public enum Filter {
        ACTIVE("Active"), OVERDUE("Overdue"), ALL("All");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final LoanRepository loans;
    private final BookRepository books;
    private final MemberRepository members;
    private final Clock clock;   // injected so tests control "today"

    public LoanService(LoanRepository loans, BookRepository books, MemberRepository members, Clock clock) {
        this.loans = loans;
        this.books = books;
        this.members = members;
        this.clock = clock;
    }

    /** Today's date according to the service clock. */
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public List<Loan> list(Filter filter) {
        return switch (filter) {
            case ALL -> loans.findAll(false);
            case ACTIVE -> loans.findAll(true);
            case OVERDUE -> loans.findAll(true).stream().filter(l -> l.isOverdue(today())).toList();
        };
    }

    /**
     * Lends a book to a member for the given number of days.
     *
     * @throws ValidationException if the book is out, the member has too
     *                             many books, or the days are out of range
     */
    public Loan checkout(long bookId, long memberId, int days) {
        Validation.requireRange(days, "Loan length (days)", 1, MAX_DAYS);
        Book book = books.findById(bookId)
                .orElseThrow(() -> new ValidationException("That book no longer exists. Refresh the list."));
        Member member = members.findById(memberId)
                .orElseThrow(() -> new ValidationException("That member no longer exists. Refresh the list."));

        if (loans.isBookOnLoan(bookId)) {
            throw new ValidationException("\"" + book.title() + "\" is already on loan.");
        }
        if (loans.countActiveByMember(memberId) >= MAX_ACTIVE_PER_MEMBER) {
            throw new ValidationException(member.fullName() + " already has " + MAX_ACTIVE_PER_MEMBER
                    + " books. They must return one first.");
        }

        LocalDate today = today();
        Loan loan = new Loan(0, bookId, memberId, book.title(), member.fullName(), today, today.plusDays(days), null);
        try {
            return loans.insert(loan);
        } catch (DuplicateKeyException ex) {
            // Someone else lent it between our check and the insert; the
            // UNIQUE index in the database caught it
            throw new ValidationException("\"" + book.title() + "\" was just lent by someone else.");
        }
    }

    /** Registers that the book of this loan came back today. */
    public void returnBook(long loanId) {
        Loan loan = loans.findById(loanId)
                .orElseThrow(() -> new ValidationException("That loan no longer exists. Refresh the list."));
        if (!loan.isActive()) {
            throw new ValidationException("That book was already returned on " + loan.returnDate() + ".");
        }
        loans.markReturned(loanId, today());
    }
}
