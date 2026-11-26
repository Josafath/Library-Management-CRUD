package shelfwise.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import shelfwise.db.ConnectionFactory;
import shelfwise.model.Role;
import shelfwise.model.User;
import shelfwise.repository.UserRepository;

/**
 * UserRepository backed by the MySQL "users" table.
 */
public final class JdbcUserRepository implements UserRepository {

    // Columns listed explicitly (no "select *") so a schema change can't silently shift them
    private static final String COLUMNS = "id, first_name, last_name, age, username, password_hash, role";

    private final ConnectionFactory connections;

    public JdbcUserRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY id";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading users", ex);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", stmt -> stmt.setLong(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE username = ?", stmt -> stmt.setString(1, username));
    }

    @Override
    public int countByRole(Role role) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();                      // COUNT(*) always returns one row
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Counting users", ex);
        }
    }

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO users (first_name, last_name, age, username, password_hash, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql, JdbcSupport.RETURN_KEYS)) {
            stmt.setString(1, user.firstName());
            stmt.setString(2, user.lastName());
            stmt.setInt(3, user.age());
            stmt.setString(4, user.username());
            stmt.setString(5, user.passwordHash());
            stmt.setString(6, user.role().name());
            stmt.executeUpdate();
            return user.withId(JdbcSupport.generatedId(stmt));
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Saving the user", ex);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, age = ?, username = ?, role = ? WHERE id = ?";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.firstName());
            stmt.setString(2, user.lastName());
            stmt.setInt(3, user.age());
            stmt.setString(4, user.username());
            stmt.setString(5, user.role().name());
            stmt.setLong(6, user.id());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Updating the user", ex);
        }
    }

    @Override
    public void updatePasswordHash(long id, String passwordHash) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            stmt.setString(1, passwordHash);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Changing the password", ex);
        }
    }

    @Override
    public void delete(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Deleting the user", ex);
        }
    }

    // Lets findById/findByUsername share the query code while setting different parameters
    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private Optional<User> findOne(String sql, Binder binder) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading the user", ex);
        }
    }

    // Converts the current row of the result set into a User
    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getInt("age"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")));
    }
}
