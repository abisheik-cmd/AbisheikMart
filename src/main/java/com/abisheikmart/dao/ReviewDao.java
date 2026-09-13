package com.abisheikmart.dao;

import com.abisheikmart.model.Review;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDao.class);

    public Review save(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, user_name, rating, comment, image_url) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setString(3, review.getUserName());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.setString(6, review.getImageUrl() != null ? review.getImageUrl() : "");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }
            return review;
        }
    }

    public List<Review> findByProductId(Long productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT id, product_id, user_id, user_name, rating, comment, COALESCE(image_url, '') AS image_url, created_at FROM reviews WHERE product_id = ? ORDER BY id DESC;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setProductId(rs.getLong("product_id"));
                    r.setUserId(rs.getLong("user_id"));
                    r.setUserName(rs.getString("user_name"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setImageUrl(rs.getString("image_url"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reviews for productId: {}", productId, e);
        }
        return list;
    }
}
