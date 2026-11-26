package shelfwise.support;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import shelfwise.Services;
import shelfwise.model.Book;
import shelfwise.model.Loan;
import shelfwise.model.Member;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.repository.BookRepository;
import shelfwise.repository.DuplicateKeyException;
import shelfwise.repository.LoanRepository;
import shelfwise.repository.MemberRepository;
import shelfwise.repository.UserRepository;
import shelfwise.security.PasswordHasher;

/**
 * The four repositories kept in plain maps, for fast unit tests without
 * MySQL. They copy the database rules the services rely on: unique
 * usernames and emails, one active loan per book, availability calculated
 * from the loans, and deletes cascading to the loan history.
 */
public final class InMemoryRepositories {

    public final Users users = new Users();
    public final Books books = new Books();
    public final Members members = new Members();
    public final Loans loans = new Loans();

    /** Services on top of these repositories; few hash rounds so tests run fast. */
    public Services services(Clock clock) {
        return Services.wire(users, books, members, loans, new PasswordHasher(1_000), clock);
    }

    private static boolean contains(String haystack, String needle) {
        return haystack.toLowerCase(Locale.ROOT).contains(needle.strip().toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------
    public final class Users implements UserRepository {
        private final Map<Long, User> rows = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public List<User> findAll() {
            return new ArrayList<>(rows.values());
        }

        @Override public Optional<User> findById(long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override public Optional<User> findByUsername(String username) {
            return rows.values().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
        }

        @Override public int countByRole(Role role) {
            return (int) rows.values().stream().filter(u -> u.role() == role).count();
        }

        @Override public User insert(User user) {
            requireUniqueUsername(user.username(), 0);
            User saved = user.withId(nextId++);
            rows.put(saved.id(), saved);
            return saved;
        }

        @Override public void update(User user) {
            requireUniqueUsername(user.username(), user.id());
            User old = rows.get(user.id());
            rows.put(user.id(), user.withPasswordHash(old.passwordHash()));   // update() never touches the hash
        }

        @Override public void updatePasswordHash(long id, String passwordHash) {
            rows.computeIfPresent(id, (k, u) -> u.withPasswordHash(passwordHash));
        }

        @Override public void delete(long id) {
            rows.remove(id);
        }

        private void requireUniqueUsername(String username, long ownId) {
            boolean taken = rows.values().stream()
                    .anyMatch(u -> u.id() != ownId && u.username().equalsIgnoreCase(username));
            if (taken) {
                throw new DuplicateKeyException("duplicate username", null);
            }
        }
    }

    // ------------------------------------------------------------------
    public final class Books implements BookRepository {
        private final Map<Long, Book> rows = new LinkedHashMap<>();
        private long nextId = 1;

        // Availability is always recalculated from the loans, like the SQL query does
        private Book withAvailability(Book b) {
            return b.withAvailable(!loans.isBookOnLoan(b.id()));
        }

        @Override public List<Book> search(String text) {
            return rows.values().stream()
                    .filter(b -> text == null || text.isBlank()
                            || contains(b.title(), text) || contains(b.author(), text)
                            || contains(b.genre(), text) || contains(b.publisher(), text))
                    .map(this::withAvailability)
                    .sorted(Comparator.comparing(Book::title))
                    .toList();
        }

        @Override public List<Book> findAvailable() {
            return search("").stream().filter(Book::available).toList();
        }

        @Override public Optional<Book> findById(long id) {
            return Optional.ofNullable(rows.get(id)).map(this::withAvailability);
        }

        @Override public Book insert(Book book) {
            Book saved = book.withId(nextId++).withAvailable(true);
            rows.put(saved.id(), saved);
            return saved;
        }

        @Override public void update(Book book) {
            rows.put(book.id(), book);
        }

        @Override public void delete(long id) {
            rows.remove(id);
            loans.rows.values().removeIf(l -> l.bookId() == id);   // ON DELETE CASCADE
        }
    }

    // ------------------------------------------------------------------
    public final class Members implements MemberRepository {
        private final Map<Long, Member> rows = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public List<Member> search(String text) {
            return rows.values().stream()
                    .filter(m -> text == null || text.isBlank()
                            || contains(m.fullName(), text) || contains(m.email(), text) || contains(m.phone(), text))
                    .sorted(Comparator.comparing(Member::fullName))
                    .toList();
        }

        @Override public Optional<Member> findById(long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override public Member insert(Member member) {
            requireUniqueEmail(member.email(), 0);
            Member saved = member.withId(nextId++);
            rows.put(saved.id(), saved);
            return saved;
        }

        @Override public void update(Member member) {
            requireUniqueEmail(member.email(), member.id());
            rows.put(member.id(), member);
        }

        @Override public void delete(long id) {
            rows.remove(id);
            loans.rows.values().removeIf(l -> l.memberId() == id);   // ON DELETE CASCADE
        }

        private void requireUniqueEmail(String email, long ownId) {
            if (rows.values().stream().anyMatch(m -> m.id() != ownId && m.email().equalsIgnoreCase(email))) {
                throw new DuplicateKeyException("duplicate email", null);
            }
        }
    }

    // ------------------------------------------------------------------
    public final class Loans implements LoanRepository {
        private final Map<Long, Loan> rows = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public List<Loan> findAll(boolean activeOnly) {
            return rows.values().stream()
                    .filter(l -> !activeOnly || l.isActive())
                    .sorted(Comparator.comparing(Loan::loanDate).thenComparing(Loan::id).reversed())
                    .toList();
        }

        @Override public Optional<Loan> findById(long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override public boolean isBookOnLoan(long bookId) {
            return rows.values().stream().anyMatch(l -> l.bookId() == bookId && l.isActive());
        }

        @Override public int countActiveByMember(long memberId) {
            return (int) rows.values().stream().filter(l -> l.memberId() == memberId && l.isActive()).count();
        }

        @Override public Loan insert(Loan loan) {
            if (isBookOnLoan(loan.bookId())) {   // like the UNIQUE(active_book_id) index
                throw new DuplicateKeyException("book already on loan", null);
            }
            Loan saved = loan.withId(nextId++);
            rows.put(saved.id(), saved);
            return saved;
        }

        @Override public void markReturned(long loanId, LocalDate returnDate) {
            rows.computeIfPresent(loanId, (k, l) -> l.isActive() ? l.returnedOn(returnDate) : l);
        }
    }
}
