package shelfwise.service;

import java.util.List;
import shelfwise.model.Book;
import shelfwise.repository.BookRepository;
import shelfwise.repository.LoanRepository;

/**
 * The book catalog: search, add, edit, delete.
 */
public final class BookService {

    private final BookRepository books;
    private final LoanRepository loans;

    public BookService(BookRepository books, LoanRepository loans) {
        this.books = books;
        this.loans = loans;
    }

    /** Books matching the text (title, author, genre or publisher); blank = all. */
    public List<Book> search(String text) {
        return books.search(text);
    }

    /** Books nobody has right now, for the "New loan" dialog. */
    public List<Book> available() {
        return books.findAvailable();
    }

    /**
     * Saves a book: inserts it when its id is 0, otherwise updates it.
     *
     * @return the saved book (with its id)
     */
    public Book save(Book book) {
        Book clean = new Book(
                book.id(),
                Validation.requireText(book.title(), "Title", 150),
                Validation.requireText(book.author(), "Author", 100),
                Validation.requireText(book.genre(), "Genre", 60),
                Validation.requireText(book.publisher(), "Publisher", 100),
                book.available());
        if (clean.id() == 0) {
            return books.insert(clean);
        }
        Book existing = books.findById(clean.id())
                .orElseThrow(() -> new ValidationException("That book no longer exists. Refresh the list."));
        books.update(clean);
        return clean.withAvailable(existing.available());
    }

    /** Deletes a book, unless someone has it right now. */
    public void delete(long id) {
        if (loans.isBookOnLoan(id)) {
            throw new ValidationException("This book is on loan. Register its return before deleting it.");
        }
        books.delete(id);
    }
}
