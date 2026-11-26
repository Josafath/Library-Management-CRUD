package shelfwise.ui;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import shelfwise.model.Book;
import shelfwise.service.BookService;
import static shelfwise.ui.ListTableModel.Column;

/**
 * "Books" tab: the catalog.
 */
public final class BooksPanel extends CrudPanel<Book> {

    private final BookService books;

    private final JTextField title = new JTextField(20);
    private final JTextField author = new JTextField(20);
    private final JTextField genre = new JTextField(20);
    private final JTextField publisher = new JTextField(20);

    public BooksPanel(BookService books) {
        super("book", List.of(
                Column.<Book>number("ID", Book::id),
                Column.<Book>text("Title", Book::title),
                Column.<Book>text("Author", Book::author),
                Column.<Book>text("Genre", Book::genre),
                Column.<Book>text("Publisher", Book::publisher),
                // Calculated from the loans, not typed by hand as in v1
                Column.<Book>text("Status", b -> b.available() ? "Available" : "On loan")));
        this.books = books;
        initUi();
    }

    @Override
    protected List<Book> load(String search) {
        return books.search(search);
    }

    @Override
    protected JComponent buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Title:", title);
        form.addRow("Author:", author);
        form.addRow("Genre:", genre);
        form.addRow("Publisher:", publisher);
        form.addFiller();
        return form;
    }

    @Override
    protected void fillForm(Book book) {
        title.setText(book.title());
        author.setText(book.author());
        genre.setText(book.genre());
        publisher.setText(book.publisher());
    }

    @Override
    protected void clearForm() {
        title.setText("");
        author.setText("");
        genre.setText("");
        publisher.setText("");
        title.requestFocusInWindow();
    }

    @Override
    protected Book save(Book current) {
        long id = current == null ? 0 : current.id();   // 0 = insert, otherwise update
        boolean available = current == null || current.available();
        return books.save(new Book(id, title.getText(), author.getText(), genre.getText(), publisher.getText(), available));
    }

    @Override
    protected void delete(Book book) {
        books.delete(book.id());
    }

    @Override
    protected long idOf(Book book) {
        return book.id();
    }

    @Override
    protected String describe(Book book) {
        return book.title();
    }

    @Override
    protected String searchHint() {
        return "Title, author, genre or publisher";
    }
}
