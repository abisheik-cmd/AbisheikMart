package com.abisheikmart.dao;

import com.abisheikmart.model.Product;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    private static final String BASE_SELECT = """
        SELECT p.id, p.seller_id, u.name AS seller_name, p.category_id, c.name AS category_name,
               p.name, p.description, p.price, p.stock, COALESCE(p.image_url, '') AS image_url, p.created_at
        FROM products p
        JOIN users u ON p.seller_id = u.id
        JOIN categories c ON p.category_id = c.id
    """;

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY p.id DESC;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
        }
        return list;
    }

    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT + " WHERE p.id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by id: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Product> findBySellerId(Long sellerId) {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.seller_id = ? ORDER BY p.id DESC;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by seller id: {}", sellerId, e);
        }
        return list;
    }

    public List<Product> findByCategoryId(Long categoryId) {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.category_id = ? ORDER BY p.id DESC;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by category id: {}", categoryId, e);
        }
        return list;
    }

    public List<Product> search(String query, String categoryName, String sortBy) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(u.name) LIKE ?) ");
        }
        if (categoryName != null && !categoryName.isBlank() && !"ALL".equalsIgnoreCase(categoryName)) {
            sql.append(" AND LOWER(c.name) = LOWER(?) ");
        }

        if ("price-low".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.price ASC ");
        } else if ("price-high".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.price DESC ");
        } else if ("name".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.name ASC ");
        } else {
            sql.append(" ORDER BY p.id DESC ");
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase().trim() + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            if (categoryName != null && !categoryName.isBlank() && !"ALL".equalsIgnoreCase(categoryName)) {
                ps.setString(paramIndex++, categoryName.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products with query: {}, category: {}", query, categoryName, e);
        }
        return list;
    }

    public Product save(Product product) throws SQLException {
        String sql = "INSERT INTO products (seller_id, category_id, name, description, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setLong(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getStock());
            ps.setString(7, product.getImageUrl() != null ? product.getImageUrl() : "");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
            return product;
        }
    }

    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, price = ?, stock = ?, image_url = ? WHERE id = ? AND seller_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.setLong(8, product.getSellerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id, Long sellerId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?" + (sellerId != null ? " AND seller_id = ?;" : ";");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (sellerId != null) {
                ps.setLong(2, sellerId);
            }
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setSellerName(rs.getString("seller_name"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
