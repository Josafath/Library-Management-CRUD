package shelfwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shelfwise.Services;
import shelfwise.model.Book;
import shelfwise.model.Member;
import shelfwise.support.InMemoryRepositories;
import shelfwise.support.MutableClock;

class MemberServiceTest {

    private Services services;
    private MemberService members;

    @BeforeEach
    void setUp() {
        services = new InMemoryRepositories().services(new MutableClock(LocalDate.of(2026, 3, 1)));
        members = services.members();
    }

    @Test
    void savesAndUpdatesAMember() {
        Member ana = members.save(Member.draft(" Ana López ", " ana@example.com ", ""));
        assertEquals("Ana López", ana.fullName());
        assertEquals("ana@example.com", ana.email());

        members.save(new Member(ana.id(), "Ana María López", "ana@example.com", "555-0101"));
        Member saved = members.search("").get(0);
        assertEquals("Ana María López", saved.fullName());
        assertEquals("555-0101", saved.phone());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@", "@example.com", "ana@example", "ana lopez@example.com"})
    void rejectsInvalidEmails(String email) {
        assertThrows(ValidationException.class, () -> members.save(Member.draft("Ana", email, "")));
    }

    @Test
    void emailMustBeUnique() {
        members.save(Member.draft("Ana", "ana@example.com", ""));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> members.save(Member.draft("Otra Ana", "ANA@example.com", "")));
        assertEquals("Another member already uses ANA@example.com.", ex.getMessage());
    }

    @Test
    void searchMatchesNameEmailAndPhone() {
        members.save(Member.draft("Ana López", "ana@example.com", "555-0101"));
        members.save(Member.draft("Carlos Ruiz", "carlos@example.com", ""));
        assertEquals(1, members.search("lópez").size());
        assertEquals(1, members.search("CARLOS@").size());
        assertEquals(1, members.search("0101").size());
        assertEquals(2, members.search("").size());
    }

    @Test
    void memberWithBooksCannotBeDeleted() {
        Member ana = members.save(Member.draft("Ana", "ana@example.com", ""));
        Book book = services.books().save(Book.draft("1984", "Orwell", "Dystopia", "Secker"));
        var loan = services.loans().checkout(book.id(), ana.id(), 7);

        ValidationException ex = assertThrows(ValidationException.class, () -> members.delete(ana.id()));
        assertTrue(ex.getMessage().contains("still has 1 book(s)"));

        services.loans().returnBook(loan.id());
        members.delete(ana.id());
        assertTrue(members.search("").isEmpty());
    }
}
