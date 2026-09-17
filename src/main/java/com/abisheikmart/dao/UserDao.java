package com.abisheikmart.dao;

import com.abisheikmart.model.User;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, role, active, created_at FROM users WHERE email = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, name, email, password_hash, role, active, created_at FROM users WHERE id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
        }
        return Optional.empty();
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking user email existence: {}", email, e);
        }
        return false;
    }

    public List<User> findAllUsers(String search, String role, Boolean active) {
        StringBuilder sql = new StringBuilder("SELECT id, name, email, password_hash, role, active, created_at FROM users WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase() + "%";
            parameters.add(pattern); parameters.add(pattern);
        }
        if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
            sql.append(" AND role = ?");
            parameters.add(role.toUpperCase());
        }
        if (active != null) {
            sql.append(" AND active = ?");
            parameters.add(active);
        }
        sql.append(" ORDER BY id DESC");
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) ps.setObject(i + 1, parameters.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding admin users", e);
        }
        return users;
    }

    public boolean updateUserStatus(Long userId, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ? AND role <> 'ADMIN'";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active); ps.setLong(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public long countUsers() { return count("SELECT COUNT(*) FROM users"); }
    public long countActiveUsers() { return count("SELECT COUNT(*) FROM users WHERE active = TRUE"); }
    public long countByRole(String role) { return countWithValue("SELECT COUNT(*) FROM users WHERE role = ?", role); }

    private long count(String sql) {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { logger.error("Error counting users", e); return 0L; }
    }

    private long countWithValue(String sql, String value) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) { logger.error("Error counting users by role", e); return 0L; }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        try { u.setActive(rs.getBoolean("active")); } catch (SQLException ignored) { u.setActive(true); }
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
