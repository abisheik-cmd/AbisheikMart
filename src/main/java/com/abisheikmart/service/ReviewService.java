package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dao.ReviewDao;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.Review;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReviewService {
    private final ReviewDao reviewDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;

    public ReviewService() { this(new ReviewDao(), new ProductDao(), new OrderDao()); }
    public ReviewService(ReviewDao reviewDao) { this(reviewDao, new ProductDao(), new OrderDao()); }
    public ReviewService(ReviewDao reviewDao, ProductDao productDao, OrderDao orderDao) { this.reviewDao = reviewDao; this.productDao = productDao; this.orderDao = orderDao; }

    public Review createReview(Long userId, String role, Long productId, int rating, String comment, String imageUrl) throws SQLException {
        requireBuyer(userId, role); validateInput(productId, userId, rating, comment);
        Product product = productDao.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (!product.getActive()) throw new IllegalArgumentException("This product is not available for review.");
        if (!orderDao.hasPurchasedProduct(userId, productId)) throw new SecurityException("You can review only products you have purchased.");
        if (reviewDao.findByUserAndProduct(userId, productId).isPresent()) throw new IllegalArgumentException("You have already reviewed this product.");
        Review review = new Review(); review.setProductId(productId); review.setUserId(userId); review.setRating(rating); review.setComment(comment.trim()); review.setImageUrl(imageUrl == null ? "" : imageUrl.trim());
        return reviewDao.createReview(review);
    }

    public boolean updateReview(Long userId, String role, Long reviewId, int rating, String comment) throws SQLException {
        requireBuyer(userId, role); validateInput(1L, userId, rating, comment);
        Review existing = reviewDao.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found."));
        if (!userId.equals(existing.getUserId())) throw new SecurityException("You may update only your own review.");
        return reviewDao.updateReview(reviewId, userId, rating, comment.trim());
    }

    public boolean deleteReview(Long userId, String role, Long reviewId) throws SQLException {
        requireBuyer(userId, role);
        Review existing = reviewDao.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found."));
        if (!userId.equals(existing.getUserId())) throw new SecurityException("You may delete only your own review.");
        return reviewDao.deleteReview(reviewId, userId);
    }

    public List<Review> getProductReviews(Long productId) { return reviewDao.findByProductId(productId); }
    public Optional<Review> getReview(Long reviewId) { return reviewDao.findById(reviewId); }
    public Optional<Review> getUserProductReview(Long userId, Long productId) { return reviewDao.findByUserAndProduct(userId, productId); }
    public boolean canReview(Long userId, String role, Long productId) {
        if (userId == null || !("CUSTOMER".equalsIgnoreCase(role) || "BUYER".equalsIgnoreCase(role))) return false;
        return productDao.findById(productId).map(Product::getActive).orElse(false) && orderDao.hasPurchasedProduct(userId, productId) && reviewDao.findByUserAndProduct(userId, productId).isEmpty();
    }
    public double getAverageRating(Long productId) { return reviewDao.getAverageRating(productId); }
    public long getReviewCount(Long productId) { return reviewDao.getReviewCount(productId); }

    private void requireBuyer(Long userId, String role) {
        if (userId == null || !("CUSTOMER".equalsIgnoreCase(role) || "BUYER".equalsIgnoreCase(role))) throw new SecurityException("Only authenticated buyers may submit reviews.");
    }

    private void validateInput(Long productId, Long userId, int rating, String comment) {
        if (productId == null || userId == null) throw new IllegalArgumentException("Product and authenticated user are required.");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        if (comment == null || comment.trim().length() < 3 || comment.trim().length() > 2000) throw new IllegalArgumentException("Review comment must be between 3 and 2000 characters.");
    }
}
