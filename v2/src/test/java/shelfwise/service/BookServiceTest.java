package shelfwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelfwise.Services;
import shelfwise.model.Book;
import shelfwise.model.Member;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class BookServiceTest {

    private InMemoryRepositories db;
    private Services services;
    private BookService books;

    @BeforeEach
    void setUp() {
        db = new InMemoryRepositories();
        services = db.services(new MutableClock(LocalDate.of(2026, 3, 1)));
        books = services.books();
    }

    private Book add(String title, String author) {
        return books.save(Book.draft(title, author, "Novel", "Editorial"));
    }

    @Test
    void newBookGetsAnIdAndIsAvailable() {
        Book b = add("Pedro Páramo", "Juan Rulfo");
        assertTrue(b.id() > 0);
        assertTrue(b.available());
    }

    @Test
    void savingWithAnIdUpdatesInsteadOfInserting() {
        Book b = add("Pedro Paramo", "Rulfo");
        books.save(new Book(b.id(), "Pedro Páramo", "Juan Rulfo", "Novel", "FCE", true));

        List<Book> all = books.search("");
        assertEquals(1, all.size());
        assertEquals("Pedro Páramo", all.get(0).title());
        assertEquals("FCE", all.get(0).publisher());
    }

    @Test
    void fieldsAreTrimmedAndRequired() {
        Book b = books.save(Book.draft("  Title ", " Author ", " Genre ", " Pub "));
        assertEquals("Title", b.title());
        assertEquals("Pub", b.publisher());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> books.save(Book.draft("Title", "   ", "Genre", "Pub")));
        assertEquals("Author is required.", ex.getMessage());
    }

    @Test
    void tooLongTitleIsRejected() {
        assertThrows(ValidationException.class, () -> books.save(Book.draft("x".repeat(151), "A", "G", "P")));
    }

    @Test
    void searchLooksInEveryTextField() {
        add("Cien años de soledad", "Gabriel García Márquez");
        add("Clean Code", "Robert C. Martin");
        assertEquals(1, books.search("garcía").size());
        assertEquals(1, books.search("CLEAN").size());
        assertEquals(2, books.search("novel").size());   // genre
        assertEquals(2, books.search("  ").size());      // blank = all
    }

    @Test
    void availabilityFollowsTheLoans() {
        Book b = add("1984", "George Orwell");
        Member m = services.members().save(Member.draft("Ana", "ana@example.com", ""));
        var loan = services.loans().checkout(b.id(), m.id(), 7);

        assertFalse(books.search("1984").get(0).available());
        assertTrue(books.available().isEmpty());

        services.loans().returnBook(loan.id());
        assertTrue(books.search("1984").get(0).available());
    }

    @Test
    void bookOnLoanCannotBeDeleted() {
        Book b = add("1984", "George Orwell");
        Member m = services.members().save(Member.draft("Ana", "ana@example.com", ""));
        services.loans().checkout(b.id(), m.id(), 7);

        ValidationException ex = assertThrows(ValidationException.class, () -> books.delete(b.id()));
        assertTrue(ex.getMessage().contains("on loan"));
    }

    @Test
    void returnedBookCanBeDeletedWithItsHistory() {
        Book b = add("1984", "George Orwell");
        Member m = services.members().save(Member.draft("Ana", "ana@example.com", ""));
        var loan = services.loans().checkout(b.id(), m.id(), 7);
        services.loans().returnBook(loan.id());

        books.delete(b.id());
        assertTrue(books.search("").isEmpty());
        assertTrue(db.loans.findAll(false).isEmpty());
    }

    @Test
    void updatingADeletedBookExplainsWhatHappened() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> books.save(new Book(42, "T", "A", "G", "P", true)));
        assertTrue(ex.getMessage().contains("no longer exists"));
    }
}
