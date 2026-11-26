package shelfwise.repository.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import shelfwise.db.ConnectionFactory;
import shelfwise.model.Loan;
import shelfwise.repository.LoanRepository;

/**
 * LoanRepository backed by the MySQL "loans" table.
 */
public final class JdbcLoanRepository implements LoanRepository {

    // JOINs bring the book title and member name so the table can show them
    private static final String SELECT =
            "SELECT l.id, l.book_id, l.member_id, b.title, m.full_name, l.loan_date, l.due_date, l.return_date "
            + "FROM loans l JOIN books b ON b.id = l.book_id JOIN members m ON m.id = l.member_id ";

    private final ConnectionFactory connections;

    public JdbcLoanRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public List<Loan> findAll(boolean activeOnly) {
        String sql = SELECT + (activeOnly ? "WHERE l.return_date IS NULL " : "") + "ORDER BY l.loan_date DESC, l.id DESC";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Loan> loans = new ArrayList<>();
            while (rs.next()) {
                loans.add(map(rs));
            }
            return loans;
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading loans", ex);
        }
    }

    @Override
    public Optional<Loan> findById(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE l.id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading the loan", ex);
        }
    }

    @Override
    public boolean isBookOnLoan(long bookId) {
        return count("SELECT COUNT(*) FROM loans WHERE book_id = ? AND return_date IS NULL", bookId) > 0;
    }

    @Override
    public int countActiveByMember(long memberId) {
        return count("SELECT COUNT(*) FROM loans WHERE member_id = ? AND return_date IS NULL", memberId);
    }

    @Override
    public Loan insert(Loan loan) {
        String sql = "INSERT INTO loans (book_id, member_id, loan_date, due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql, JdbcSupport.RETURN_KEYS)) {
            stmt.setLong(1, loan.bookId());
            stmt.setLong(2, loan.memberId());
            stmt.setDate(3, Date.valueOf(loan.loanDate()));
            stmt.setDate(4, Date.valueOf(loan.dueDate()));
            stmt.executeUpdate();   // the UNIQUE active_book_id refuses a book that is already out
            return loan.withId(JdbcSupport.generatedId(stmt));
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Saving the loan", ex);
        }
    }

    @Override
    public void markReturned(long loanId, LocalDate returnDate) {
        // "return_date IS NULL" makes returning the same loan twice a no-op
        String sql = "UPDATE loans SET return_date = ? WHERE id = ? AND return_date IS NULL";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setLong(2, loanId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Returning the book", ex);
        }
    }

    private int count(String sql, long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Counting loans", ex);
        }
    }

    private static Loan map(ResultSet rs) throws SQLException {
        Date returned = rs.getDate("return_date");   // NULL while the book is out
        return new Loan(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getLong("member_id"),
                rs.getString("title"),
                rs.getString("full_name"),
                rs.getDate("loan_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returned == null ? null : returned.toLocalDate());
    }
}
