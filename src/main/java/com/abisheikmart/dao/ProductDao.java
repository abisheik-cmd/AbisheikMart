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
               p.name, p.description, COALESCE(p.original_price, p.price) AS original_price, p.price, p.stock,
               COALESCE(p.image_url, '') AS image_url, p.active, p.created_at,
               COALESCE(AVG(r.rating), 0.0) AS avg_rating,
               COUNT(r.id) AS review_count
        FROM products p
        JOIN users u ON p.seller_id = u.id
        JOIN categories c ON p.category_id = c.id
        LEFT JOIN reviews r ON p.id = r.product_id
    """;

    private static final String GROUP_BY = " GROUP BY p.id, p.seller_id, u.name, p.category_id, c.name, p.name, p.description, p.original_price, p.price, p.stock, p.image_url, p.active, p.created_at ";

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.active = TRUE" + GROUP_BY + " ORDER BY p.id DESC;";
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
        String sql = BASE_SELECT + " WHERE p.id = ?" + GROUP_BY + ";";
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
        String sql = BASE_SELECT + " WHERE p.seller_id = ?" + GROUP_BY + " ORDER BY p.id DESC;";
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

    public long countBySellerId(Long sellerId) {
        return count("SELECT COUNT(*) FROM products WHERE seller_id = ?", sellerId);
    }

    public long countActiveBySellerId(Long sellerId) {
        // This schema has no status column; every persisted product is an active listing.
        return countBySellerId(sellerId);
    }

    public List<Product> findAdminProducts(String search, Long categoryId, Long sellerId, Boolean active) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(u.name) LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase() + "%";
            parameters.add(pattern); parameters.add(pattern); parameters.add(pattern);
        }
        if (categoryId != null) { sql.append(" AND p.category_id = ?"); parameters.add(categoryId); }
        if (sellerId != null) { sql.append(" AND p.seller_id = ?"); parameters.add(sellerId); }
        if (active != null) { sql.append(" AND p.active = ?"); parameters.add(active); }
        sql.append(GROUP_BY).append(" ORDER BY p.id DESC");
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) ps.setObject(i + 1, parameters.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) products.add(mapProduct(rs)); }
        } catch (SQLException e) { logger.error("Error finding admin products", e); }
        return products;
    }

    public boolean updateProductStatus(Long productId, boolean active) throws SQLException {
        String sql = "UPDATE products SET active = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active); ps.setLong(2, productId);
            return ps.executeUpdate() == 1;
        }
    }

    public long countProducts() { return countProducts("SELECT COUNT(*) FROM products"); }
    public long countActiveProducts() { return countProducts("SELECT COUNT(*) FROM products WHERE active = TRUE"); }
    public long countInactiveProducts() { return countProducts("SELECT COUNT(*) FROM products WHERE active = FALSE"); }

    private long countProducts(String sql) {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) { logger.error("Error counting products", e); return 0L; }
    }

    private long count(String sql, Long sellerId) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            logger.error("Error counting seller products for sellerId: {}", sellerId, e);
            return 0L;
        }
    }

    public List<Product> findByCategoryId(Long categoryId) {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.category_id = ? AND p.active = TRUE" + GROUP_BY + " ORDER BY p.id DESC;";
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

    public List<Product> findRecommendations(Long limit) {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.active = TRUE" + GROUP_BY + " ORDER BY avg_rating DESC, p.id DESC LIMIT ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, limit != null ? limit : 6);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding recommendation products", e);
        }
        return list;
    }

    public List<Product> search(String query, String categoryName, String sortBy) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE p.active = TRUE ");

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(u.name) LIKE ?) ");
        }
        if (categoryName != null && !categoryName.isBlank() && !"ALL".equalsIgnoreCase(categoryName)) {
            sql.append(" AND LOWER(c.name) = LOWER(?) ");
        }

        sql.append(GROUP_BY);

        if ("price-low".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.price ASC ");
        } else if ("price-high".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.price DESC ");
        } else if ("name".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.name ASC ");
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY avg_rating DESC ");
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
        String sql = "INSERT INTO products (seller_id, category_id, name, description, original_price, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setLong(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
            ps.setDouble(6, product.getPrice());
            ps.setInt(7, product.getStock());
            ps.setString(8, product.getImageUrl() != null ? product.getImageUrl() : "");
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
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, original_price = ?, price = ?, stock = ?, image_url = ? WHERE id = ? AND seller_id = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getOriginalPrice());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getStock());
            ps.setString(7, product.getImageUrl());
            ps.setLong(8, product.getId());
            ps.setLong(9, product.getSellerId());
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
        p.setOriginalPrice(rs.getDouble("original_price"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        try { p.setActive(rs.getBoolean("active")); } catch (SQLException ignored) { p.setActive(true); }
        p.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            p.setAverageRating(rs.getDouble("avg_rating"));
            p.setReviewCount(rs.getInt("review_count"));
        } catch (SQLException ignored) {}
        return p;
    }
}
