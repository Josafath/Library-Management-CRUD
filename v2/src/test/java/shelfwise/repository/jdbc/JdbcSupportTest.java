package shelfwise.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import shelfwise.repository.DataAccessException;
import shelfwise.repository.DuplicateKeyException;

class JdbcSupportTest {

    @Test
    void likePatternWrapsAndLowercases() {
        assertEquals("%garcía%", JdbcSupport.likePattern("  García "));
    }

    @Test
    void likeWildcardsTypedByTheUserAreSearchedLiterally() {
        // Without escaping, "100%" would match everything starting with "100"
        assertEquals("%100!%%", JdbcSupport.likePattern("100%"));
        assertEquals("%a!_b%", JdbcSupport.likePattern("a_b"));
        assertEquals("%wow!!%", JdbcSupport.likePattern("wow!"));
    }

    @Test
    void duplicateEntryBecomesDuplicateKeyException() {
        SQLException dup = new SQLException("Duplicate entry 'ana' for key 'uq_users_username'", "23000", 1062);
        assertInstanceOf(DuplicateKeyException.class, JdbcSupport.translate("Saving", dup));
    }

    @Test
    void otherErrorsKeepTheirMessage() {
        SQLException other = new SQLException("Access denied", "28000", 1045);
        DataAccessException ex = JdbcSupport.translate("Loading", other);
        assertEquals(DataAccessException.class, ex.getClass());
        assertEquals("Loading: Access denied", ex.getMessage());
    }
}
