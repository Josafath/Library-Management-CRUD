package shelfwise.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import shelfwise.db.ConnectionFactory;
import shelfwise.model.Member;
import shelfwise.repository.MemberRepository;

/**
 * MemberRepository backed by the MySQL "members" table.
 */
public final class JdbcMemberRepository implements MemberRepository {

    private static final String SELECT = "SELECT id, full_name, email, phone FROM members ";

    private final ConnectionFactory connections;

    public JdbcMemberRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public List<Member> search(String text) {
        boolean filtered = text != null && !text.isBlank();
        String sql = SELECT
                + (filtered ? "WHERE LOWER(full_name) LIKE ? ESCAPE '!' OR LOWER(email) LIKE ? ESCAPE '!' OR LOWER(phone) LIKE ? ESCAPE '!' " : "")
                + "ORDER BY full_name";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtered) {
                String pattern = JdbcSupport.likePattern(text);
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Member> members = new ArrayList<>();
                while (rs.next()) {
                    members.add(map(rs));
                }
                return members;
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading members", ex);
        }
    }

    @Override
    public Optional<Member> findById(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Loading the member", ex);
        }
    }

    @Override
    public Member insert(Member member) {
        String sql = "INSERT INTO members (full_name, email, phone) VALUES (?, ?, ?)";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql, JdbcSupport.RETURN_KEYS)) {
            stmt.setString(1, member.fullName());
            stmt.setString(2, member.email());
            stmt.setString(3, member.phone());
            stmt.executeUpdate();
            return member.withId(JdbcSupport.generatedId(stmt));
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Saving the member", ex);
        }
    }

    @Override
    public void update(Member member) {
        String sql = "UPDATE members SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.fullName());
            stmt.setString(2, member.email());
            stmt.setString(3, member.phone());
            stmt.setLong(4, member.id());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Updating the member", ex);
        }
    }

    @Override
    public void delete(long id) {
        try (Connection conn = connections.open();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM members WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();   // ON DELETE CASCADE removes their loan history
        } catch (SQLException ex) {
            throw JdbcSupport.translate("Deleting the member", ex);
        }
    }

    private static Member map(ResultSet rs) throws SQLException {
        return new Member(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone"));
    }
}
