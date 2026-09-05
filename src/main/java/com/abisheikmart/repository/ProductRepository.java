package com.abisheikmart.repository;

import com.abisheikmart.model.Product;
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
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setSellerName(rs.getString("seller_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    };

    public List<Product> findAll() {
        String sql = """
            SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
            FROM products p JOIN users u ON p.seller_id = u.id
            ORDER BY p.id DESC;
        """;
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public Optional<Product> findById(Long id) {
        String sql = """
            SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
            FROM products p JOIN users u ON p.seller_id = u.id
            WHERE p.id = ?;
        """;
        List<Product> list = jdbcTemplate.query(sql, productRowMapper, id);
        return list.stream().findFirst();
    }

    public List<Product> findBySellerId(Long sellerId) {
        String sql = """
            SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at
            FROM products p JOIN users u ON p.seller_id = u.id
            WHERE p.seller_id = ?
            ORDER BY p.id DESC;
        """;
        return jdbcTemplate.query(sql, productRowMapper, sellerId);
    }

    public Product save(Product product) {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl() != null ? product.getImageUrl() : "");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            product.setId(keyHolder.getKey().longValue());
        }
        return product;
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category = ?, image_url = ? WHERE id = ? AND seller_id = ?;";
        int rows = jdbcTemplate.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl(),
                product.getId(),
                product.getSellerId()
        );
        return rows > 0;
    }

    public boolean delete(Long id, Long sellerId) {
        String sql = "DELETE FROM products WHERE id = ? AND seller_id = ?;";
        return jdbcTemplate.update(sql, id, sellerId) > 0;
    }
}

