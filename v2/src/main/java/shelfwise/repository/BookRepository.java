package shelfwise.repository;

import java.util.List;
import java.util.Optional;
import shelfwise.model.Book;

/**
 * Storage of the book catalog.
 */
public interface BookRepository {

    /**
     * Books whose title, author, genre or publisher contain the text
     * (case-insensitive), ordered by title. A blank text returns every book.
     */
    List<Book> search(String text);

    /** Books with no active loan, ordered by title (for the "New loan" dialog). */
    List<Book> findAvailable();

    Optional<Book> findById(long id);

    /** @return the same book with its new id */
    Book insert(Book book);

    /** Saves title, author, genre and publisher. */
    void update(Book book);

    /** Deletes the book and its returned-loan history. */
    void delete(long id);
}
