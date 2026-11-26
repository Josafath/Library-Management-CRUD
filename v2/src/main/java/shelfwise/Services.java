package shelfwise;

import java.time.Clock;
import shelfwise.db.ConnectionFactory;
import shelfwise.repository.BookRepository;
import shelfwise.repository.LoanRepository;
import shelfwise.repository.MemberRepository;
import shelfwise.repository.UserRepository;
import shelfwise.repository.jdbc.JdbcBookRepository;
import shelfwise.repository.jdbc.JdbcLoanRepository;
import shelfwise.repository.jdbc.JdbcMemberRepository;
import shelfwise.repository.jdbc.JdbcUserRepository;
import shelfwise.security.LoginThrottle;
import shelfwise.security.PasswordHasher;
import shelfwise.service.AuthService;
import shelfwise.service.BookService;
import shelfwise.service.LoanService;
import shelfwise.service.MemberService;
import shelfwise.service.UserService;

/**
 * Every service the windows need, wired together in one place.
 *
 * The windows receive this object instead of a raw java.sql.Connection
 * (as in v1), so the UI never writes SQL.
 */
public record Services(AuthService auth, UserService users, BookService books,
                       MemberService members, LoanService loans) {

    /** Services backed by MySQL. */
    public static Services mysql(ConnectionFactory connections, Clock clock) {
        return wire(new JdbcUserRepository(connections), new JdbcBookRepository(connections),
                new JdbcMemberRepository(connections), new JdbcLoanRepository(connections),
                new PasswordHasher(), clock);
    }

    /** Builds the services on top of any repositories (tests pass in-memory ones). */
    public static Services wire(UserRepository userRepo, BookRepository bookRepo, MemberRepository memberRepo,
                                LoanRepository loanRepo, PasswordHasher hasher, Clock clock) {
        return new Services(
                new AuthService(userRepo, hasher, new LoginThrottle(clock)),
                new UserService(userRepo, hasher),
                new BookService(bookRepo, loanRepo),
                new MemberService(memberRepo, loanRepo),
                new LoanService(loanRepo, bookRepo, memberRepo, clock));
    }
}
