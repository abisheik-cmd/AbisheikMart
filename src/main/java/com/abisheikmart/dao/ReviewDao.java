package com.abisheikmart.dao;

import com.abisheikmart.model.Review;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDao {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDao.class);

    public Review save(Review review) throws SQLException { return createReview(review); }

    public Review createReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment, image_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId()); ps.setLong(2, review.getUserId()); ps.setInt(3, review.getRating()); ps.setString(4, review.getComment()); ps.setString(5, review.getImageUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) review.setId(keys.getLong(1)); }
            return findById(review.getId()).orElse(review);
        }
    }

    public Optional<Review> findById(Long id) { return queryOne("WHERE r.id = ?", id); }

    public List<Review> findByProductId(Long productId) { return queryMany("WHERE r.product_id = ? ORDER BY r.created_at DESC, r.id DESC", productId); }

    public Optional<Review> findByUserAndProduct(Long userId, Long productId) {
        String sql = baseSelect() + " WHERE r.user_id = ? AND r.product_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId); ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapReview(rs)) : Optional.empty(); }
        } catch (SQLException e) { logger.error("Error finding review for user/product", e); return Optional.empty(); }
    }

    public List<Review> findByUserId(Long userId) { return queryMany("WHERE r.user_id = ? ORDER BY r.created_at DESC, r.id DESC", userId); }

    public boolean updateReview(Long reviewId, Long userId, int rating, String comment) throws SQLException {
        String sql = "UPDATE reviews SET rating = ?, comment = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating); ps.setString(2, comment); ps.setLong(3, reviewId); ps.setLong(4, userId); return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteReview(Long reviewId, Long userId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reviewId); ps.setLong(2, userId); return ps.executeUpdate() == 1;
        }
    }

    public double getAverageRating(Long productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) FROM reviews WHERE product_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) { ps.setLong(1, productId); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0.0; } }
        catch (SQLException e) { logger.error("Error calculating rating for productId: {}", productId, e); return 0.0; }
    }

    public long getReviewCount(Long productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) { ps.setLong(1, productId); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; } }
        catch (SQLException e) { logger.error("Error counting reviews for productId: {}", productId, e); return 0L; }
    }

    private Optional<Review> queryOne(String predicate, Long id) {
        String sql = baseSelect() + " " + predicate;
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) { ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapReview(rs)) : Optional.empty(); } }
        catch (SQLException e) { logger.error("Error finding review", e); return Optional.empty(); }
    }

    private List<Review> queryMany(String predicate, Long id) {
        List<Review> reviews = new ArrayList<>(); String sql = baseSelect() + " " + predicate;
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) { ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) reviews.add(mapReview(rs)); } }
        catch (SQLException e) { logger.error("Error finding reviews", e); }
        return reviews;
    }

    private String baseSelect() {
        return "SELECT r.id, r.product_id, r.user_id, u.name AS reviewer_name, r.rating, r.comment, COALESCE(r.image_url, '') AS image_url, r.created_at, r.updated_at FROM reviews r JOIN users u ON u.id = r.user_id";
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review review = new Review(); review.setId(rs.getLong("id")); review.setProductId(rs.getLong("product_id")); review.setUserId(rs.getLong("user_id"));
        review.setUserName(rs.getString("reviewer_name")); review.setRating(rs.getInt("rating")); review.setComment(rs.getString("comment")); review.setImageUrl(rs.getString("image_url"));
        review.setCreatedAt(rs.getTimestamp("created_at")); review.setUpdatedAt(rs.getTimestamp("updated_at")); return review;
    }
}
