package shelfwise.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import shelfwise.db.ConnectionFactory;
import shelfwise.model.Book;
import shelfwise.repository.BookRepository;

/**
 * BookRepository backed by the MySQL "books" table.
 */
public final class JdbcBookRepository implements BookRepository {

    // "available" is calculated: a book is available when no loan of it is still open
    private static final String SELECT =
            "SELECT b.id, b.title, b.author, b.genre, b.publisher, "
            + "NOT EXISTS (SELECT 1 FROM loans l WHERE l.book_id = b.id AND l.return_date IS NULL) AS available "
            + "FROM books b ";

    private final ConnectionFactory connections;

    public JdbcBookRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public List<Book> search(String text) {
        if (text == null || text.isBlank()) {
            return query(SELECT + "ORDER BY b.title", null);
        }
        // LOWER(...) LIKE keeps the search case-insensitive with any collation
        String sql = SELECT
                + "WHERE LOWER(b.title) LIKE ? ESCAPE '!' OR LOWER(b.author) LIKE ? ESCAPE '!' "
                + "OR LOWER(b.genre) LIKE ? ESCAPE '!' OR LOWER(b.publisher) LIKE ? ESCAPE '!' "
                + "ORDER BY b.title";
        return query(sql, JdbcSupport.likePattern(text));
    }

    @Override
    public List<Book> findAvailable() {
        return query("SELECT * FROM (" + SELECT + ") AS t WHERE t.available ORDER BY t.title", null);
    }

    @Override
    public Optional<Book> findById(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE b.id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading the book", ex);
        }
    }

    @Override
    public Book insert(Book book) {
        String sql = "INSERT INTO books (title, author, genre, publisher) VALUES (?, ?, ?, ?)";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql, JdbcSupport.RETURN_KEYS)) {
            bindFields(stmt, book);
            stmt.executeUpdate();
            // A new book has no loans, so it starts available
            return book.withId(JdbcSupport.generatedId(stmt)).withAvailable(true);
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Saving the book", ex);
        }
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, publisher = ? WHERE id = ?";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindFields(stmt, book);
            stmt.setLong(5, book.id());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Updating the book", ex);
        }
    }

    @Override
    public void delete(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();   // ON DELETE CASCADE removes its loan history
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Deleting the book", ex);
        }
    }

    // Runs a list query; pattern (if not null) fills the 4 "?" of the search
    private List<Book> query(String sql, String pattern) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (pattern != null) {
                for (int i = 1; i <= 4; i++) {   // title, author, genre, publisher
                    stmt.setString(i, pattern);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(map(rs));
                }
                return books;
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading books", ex);
        }
    }

    private static void bindFields(PreparedStatement stmt, Book book) throws SQLException {
        stmt.setString(1, book.title());
        stmt.setString(2, book.author());
        stmt.setString(3, book.genre());
        stmt.setString(4, book.publisher());
    }

    private static Book map(ResultSet rs) throws SQLException {
        return new Book(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getString("publisher"),
                rs.getBoolean("available"));
    }
}
