package shelfwise.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import shelfwise.model.Loan;

/**
 * Storage of loans.
 */
public interface LoanRepository {

    /**
     * Loans with book title and member name filled in, newest first.
     *
     * @param activeOnly true to skip loans that were already returned
     */
    List<Loan> findAll(boolean activeOnly);

    Optional<Loan> findById(long id);

    /** True if the book is currently lent to someone. */
    boolean isBookOnLoan(long bookId);

    /** How many books the member has right now. */
    int countActiveByMember(long memberId);

    /**
     * Saves a new loan.
     *
     * @return the same loan with its new id
     * @throws DuplicateKeyException if the book is already on loan
     */
    Loan insert(Loan loan);

    /** Sets the return date of an active loan. */
    void markReturned(long loanId, LocalDate returnDate);
}
