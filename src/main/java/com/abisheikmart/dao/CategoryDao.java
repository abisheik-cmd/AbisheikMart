package com.abisheikmart.dao;

import com.abisheikmart.model.Category;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name ASC;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all categories", e);
        }
        return list;
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT id, name, description FROM categories WHERE id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, description FROM categories WHERE LOWER(name) = LOWER(?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by name: {}", name, e);
        }
        return Optional.empty();
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}
