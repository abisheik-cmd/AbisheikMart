package com.abisheikmart.service;

import com.abisheikmart.dao.ReviewDao;
import com.abisheikmart.model.Review;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao;

    public ReviewService() {
        this.reviewDao = new ReviewDao();
    }

    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public Review addReview(Long productId, Long userId, String userName, int rating, String comment, String imageUrl) throws SQLException {
        if (productId == null || userId == null || userName == null || comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Invalid review data");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setUserName(userName);
        r.setRating(rating);
        r.setComment(comment.trim());
        r.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
        return reviewDao.save(r);
    }

    public List<Review> getProductReviews(Long productId) {
        return reviewDao.findByProductId(productId);
    }
}
