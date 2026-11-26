package shelfwise.model;

import java.time.LocalDate;

/**
 * A book lent to a member.
 *
 * @param id         database id; 0 means "not saved yet"
 * @param bookId     the borrowed book
 * @param memberId   who borrowed it
 * @param bookTitle  book title, for display (filled in when reading)
 * @param memberName member name, for display (filled in when reading)
 * @param loanDate   day it was lent
 * @param dueDate    day it must come back
 * @param returnDate day it came back, or null while it is still out
 */
public record Loan(long id, long bookId, long memberId, String bookTitle, String memberName,
                   LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {

    /** True while the book has not been returned. */
    public boolean isActive() {
        return returnDate == null;
    }

    /** True when the book is still out after its due date. */
    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(dueDate);
    }

    /** Copy of this loan with a database id (used after an insert). */
    public Loan withId(long newId) {
        return new Loan(newId, bookId, memberId, bookTitle, memberName, loanDate, dueDate, returnDate);
    }

    /** Copy of this loan marked as returned on the given day. */
    public Loan returnedOn(LocalDate day) {
        return new Loan(id, bookId, memberId, bookTitle, memberName, loanDate, dueDate, day);
    }
}
