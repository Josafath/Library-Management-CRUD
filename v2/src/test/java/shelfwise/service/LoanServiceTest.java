package shelfwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelfwise.Services;
import shelfwise.model.Book;
import shelfwise.model.Loan;
import shelfwise.model.Member;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class LoanServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 3, 1);

    private final MutableClock clock = new MutableClock(START);
    private InMemoryRepositories db;
    private Services services;
    private LoanService loans;
    private Member ana;

    @BeforeEach
    void setUp() {
        db = new InMemoryRepositories();
        services = db.services(clock);
        loans = services.loans();
        ana = services.members().save(Member.draft("Ana López", "ana@example.com", ""));
    }

    private Book book(String title) {
        return services.books().save(Book.draft(title, "Author", "Genre", "Publisher"));
    }

    private static String message(Runnable action) {
        return assertThrows(ValidationException.class, action::run).getMessage();
    }

    @Test
    void checkoutSetsTheDatesAndNames() {
        Book b = book("1984");
        Loan loan = loans.checkout(b.id(), ana.id(), 14);

        assertTrue(loan.id() > 0);
        assertEquals(START, loan.loanDate());
        assertEquals(START.plusDays(14), loan.dueDate());
        assertNull(loan.returnDate());
        assertEquals("1984", loan.bookTitle());
        assertEquals("Ana López", loan.memberName());
    }

    @Test
    void aBookCannotBeLentTwice() {
        Book b = book("1984");
        Member carlos = services.members().save(Member.draft("Carlos", "carlos@example.com", ""));
        loans.checkout(b.id(), ana.id(), 14);
        assertEquals("\"1984\" is already on loan.", message(() -> loans.checkout(b.id(), carlos.id(), 14)));
    }

    @Test
    void raceWithAnotherLibrarianIsCaughtByTheUniqueIndex() {
        Book b = book("1984");
        // Simulate another librarian lending the book after our check: insert the loan directly
        db.loans.insert(new Loan(0, b.id(), ana.id(), "1984", "Ana López", START, START.plusDays(7), null));
        // The service sees it on loan before trying (normal path)...
        assertTrue(message(() -> loans.checkout(b.id(), ana.id(), 7)).contains("already on loan"));
        // ...and the repository itself refuses a second active loan (the race path)
        assertThrows(DuplicateKeyException.class, () -> db.loans.insert(
                new Loan(0, b.id(), ana.id(), "1984", "Ana López", START, START.plusDays(7), null)));
    }

    @Test
    void aMemberCanHaveAtMostThreeBooks() {
        for (int i = 1; i <= LoanService.MAX_ACTIVE_PER_MEMBER; i++) {
            loans.checkout(book("Book " + i).id(), ana.id(), 14);
        }
        Book fourth = book("Book 4");
        assertTrue(message(() -> loans.checkout(fourth.id(), ana.id(), 14)).contains("already has 3 books"));
    }

    @Test
    void loanLengthMustBeBetweenOneAndSixtyDays() {
        Book b = book("1984");
        assertThrows(ValidationException.class, () -> loans.checkout(b.id(), ana.id(), 0));
        assertThrows(ValidationException.class, () -> loans.checkout(b.id(), ana.id(), LoanService.MAX_DAYS + 1));
        loans.checkout(b.id(), ana.id(), LoanService.MAX_DAYS);   // the limit itself is allowed
    }

    @Test
    void missingBookOrMemberIsExplained() {
        Book b = book("1984");
        assertTrue(message(() -> loans.checkout(999, ana.id(), 7)).contains("book no longer exists"));
        assertTrue(message(() -> loans.checkout(b.id(), 999, 7)).contains("member no longer exists"));
    }

    @Test
    void returnSetsTodayAndFreesTheBook() {
        Book b = book("1984");
        Loan loan = loans.checkout(b.id(), ana.id(), 14);
        clock.advanceDays(5);
        loans.returnBook(loan.id());

        Loan saved = db.loans.findById(loan.id()).orElseThrow();
        assertEquals(START.plusDays(5), saved.returnDate());
        assertFalse(db.loans.isBookOnLoan(b.id()));
        loans.checkout(b.id(), ana.id(), 14);   // can be lent again
    }

    @Test
    void returningTwiceIsRejected() {
        Loan loan = loans.checkout(book("1984").id(), ana.id(), 14);
        loans.returnBook(loan.id());
        assertTrue(message(() -> loans.returnBook(loan.id())).startsWith("That book was already returned"));
    }

    @Test
    void filtersShowActiveOverdueAndAll() {
        Loan returned = loans.checkout(book("Returned").id(), ana.id(), 7);
        loans.returnBook(returned.id());
        loans.checkout(book("Short").id(), ana.id(), 3);
        loans.checkout(book("Long").id(), ana.id(), 30);

        assertEquals(3, loans.list(LoanService.Filter.ALL).size());
        assertEquals(2, loans.list(LoanService.Filter.ACTIVE).size());
        assertTrue(loans.list(LoanService.Filter.OVERDUE).isEmpty());

        clock.advanceDays(4);   // the 3-day loan is now late, the 30-day one is not
        var overdue = loans.list(LoanService.Filter.OVERDUE);
        assertEquals(1, overdue.size());
        assertEquals("Short", overdue.get(0).bookTitle());
    }

    @Test
    void dueDayItselfIsNotOverdue() {
        Loan loan = loans.checkout(book("1984").id(), ana.id(), 7);
        clock.advanceDays(7);
        assertFalse(loan.isOverdue(loans.today()));
        clock.advanceDays(1);
        assertTrue(loan.isOverdue(loans.today()));
    }
}
