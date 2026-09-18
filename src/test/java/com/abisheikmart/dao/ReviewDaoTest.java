package com.abisheikmart.dao;

import com.abisheikmart.model.Review;
import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDaoTest {
    private static final ReviewDao REVIEW_DAO = new ReviewDao();

    @BeforeAll
    static void initializeDatabase() throws Exception {
        Properties props = new Properties(); props.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); props.setProperty("db.driver", "org.h2.Driver"); props.setProperty("db.username", "sa"); props.setProperty("db.password", "");
        DBUtil.initDataSource(props);
        try (InputStream input = ReviewDaoTest.class.getClassLoader().getResourceAsStream("schema.sql"); Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            assertNotNull(input); String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : schema.split(";")) if (!sql.trim().isEmpty()) try { statement.execute(sql); } catch (SQLException ignored) { }
        }
    }

    @BeforeEach
    void seedFixture() throws Exception {
        try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM reviews"); statement.executeUpdate("DELETE FROM products"); statement.executeUpdate("DELETE FROM categories"); statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("INSERT INTO users (id, name, email, password_hash, role, active) VALUES (1, 'Buyer One', 'one@test', 'hash', 'CUSTOMER', TRUE), (2, 'Buyer Two', 'two@test', 'hash', 'CUSTOMER', TRUE)");
            statement.executeUpdate("INSERT INTO categories (id, name, description) VALUES (1, 'Books', 'Books')");
            statement.executeUpdate("INSERT INTO products (id, seller_id, category_id, name, description, original_price, price, stock, image_url, active) VALUES (1, 1, 1, 'Test Book', 'A book', 20, 15, 4, '', TRUE)");
        }
    }

    @AfterAll static void closeDatabase() { DBUtil.closeDataSource(); }

    private Review review(long userId, int rating, String comment) { Review r = new Review(); r.setProductId(1L); r.setUserId(userId); r.setRating(rating); r.setComment(comment); r.setImageUrl(""); return r; }

    @Test
    void createFindUpdateAndDeleteReview() throws Exception {
        Review saved = REVIEW_DAO.createReview(review(1L, 5, "Great book"));
        assertNotNull(saved.getId()); assertEquals("Buyer One", saved.getUserName()); assertNotNull(saved.getUpdatedAt());
        assertTrue(REVIEW_DAO.findById(saved.getId()).isPresent()); assertEquals(1, REVIEW_DAO.findByProductId(1L).size()); assertTrue(REVIEW_DAO.findByUserAndProduct(1L, 1L).isPresent()); assertEquals(1, REVIEW_DAO.findByUserId(1L).size());
        assertTrue(REVIEW_DAO.updateReview(saved.getId(), 1L, 4, "Updated book review")); assertEquals("Updated book review", REVIEW_DAO.findById(saved.getId()).orElseThrow().getComment());
        assertFalse(REVIEW_DAO.updateReview(saved.getId(), 2L, 1, "IDOR attempt")); assertTrue(REVIEW_DAO.deleteReview(saved.getId(), 1L)); assertTrue(REVIEW_DAO.findByProductId(1L).isEmpty());
    }

    @Test
    void duplicateUserProductIsRejectedAndMultipleUsersAggregate() throws Exception {
        REVIEW_DAO.createReview(review(1L, 5, "Excellent")); REVIEW_DAO.createReview(review(2L, 3, "Okay"));
        assertThrows(SQLException.class, () -> REVIEW_DAO.createReview(review(1L, 4, "Duplicate")));
        assertEquals(4.0, REVIEW_DAO.getAverageRating(1L)); assertEquals(2L, REVIEW_DAO.getReviewCount(1L)); assertEquals(0.0, REVIEW_DAO.getAverageRating(99L)); assertEquals(0L, REVIEW_DAO.getReviewCount(99L));
    }
}
