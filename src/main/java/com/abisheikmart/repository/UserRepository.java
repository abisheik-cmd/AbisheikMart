package com.abisheikmart.repository;

import com.abisheikmart.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getString("role"),
            rs.getString("created_at")
    );

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? OR email = ?;";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, email, email);
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?;";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getRole());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }
        return user;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?;";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}

