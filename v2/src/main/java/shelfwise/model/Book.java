package shelfwise.model;

/**
 * A book in the catalog.
 *
 * Unlike v1, "available" is not a column someone can set by hand: it is
 * calculated from the loans table (a book is available when it has no
 * active loan), so it can never disagree with reality.
 *
 * @param id        database id; 0 means "not saved yet"
 * @param title     book title
 * @param author    author name
 * @param genre     genre, e.g. "Novel"
 * @param publisher publishing house
 * @param available true when nobody has it on loan (read-only, ignored on save)
 */
public record Book(long id, String title, String author, String genre,
                   String publisher, boolean available) {

    /** A book that hasn't been saved yet. */
    public static Book draft(String title, String author, String genre, String publisher) {
        return new Book(0, title, author, genre, publisher, true);
    }

    /** Copy of this book with a database id (used after an insert). */
    public Book withId(long newId) {
        return new Book(newId, title, author, genre, publisher, available);
    }

    /** Copy of this book with another availability (used by the in-memory test repository). */
    public Book withAvailable(boolean nowAvailable) {
        return new Book(id, title, author, genre, publisher, nowAvailable);
    }

    // Used by the "New loan" combo box
    @Override
    public String toString() {
        return title + " — " + author;
    }
}
