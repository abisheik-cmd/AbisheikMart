package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dao.ReviewDao;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {
    private ReviewDao reviewDao;
    private ProductDao productDao;
    private OrderDao orderDao;
    private ReviewService reviewService;
    private Product product;

    @BeforeEach
    void setUp() {
        reviewDao = mock(ReviewDao.class); productDao = mock(ProductDao.class); orderDao = mock(OrderDao.class);
        reviewService = new ReviewService(reviewDao, productDao, orderDao);
        product = new Product(); product.setId(1L); product.setActive(true);
        when(productDao.findById(1L)).thenReturn(Optional.of(product));
        when(orderDao.hasPurchasedProduct(2L, 1L)).thenReturn(true);
        when(reviewDao.findByUserAndProduct(2L, 1L)).thenReturn(Optional.empty());
    }

    @Test
    void validPurchasedBuyerCanCreateReview() throws SQLException {
        when(reviewDao.createReview(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Review saved = reviewService.createReview(2L, "CUSTOMER", 1L, 5, "Excellent product", "");
        assertEquals(1L, saved.getProductId()); assertEquals(2L, saved.getUserId()); assertEquals(5, saved.getRating());
        verify(reviewDao).createReview(any(Review.class));
    }

    @Test
    void sellerAdminAndUnauthenticatedUsersCannotCreate() {
        assertThrows(SecurityException.class, () -> reviewService.createReview(2L, "SELLER", 1L, 5, "Good product", ""));
        assertThrows(SecurityException.class, () -> reviewService.createReview(2L, "ADMIN", 1L, 5, "Good product", ""));
        assertThrows(SecurityException.class, () -> reviewService.createReview(null, "CUSTOMER", 1L, 5, "Good product", ""));
        verifyNoInteractions(productDao, orderDao, reviewDao);
    }

    @Test
    void invalidRatingCommentDuplicateAndUnpurchasedAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(2L, "CUSTOMER", 1L, 6, "Good product", ""));
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(2L, "CUSTOMER", 1L, 5, "x", ""));
        when(reviewDao.findByUserAndProduct(2L, 1L)).thenReturn(Optional.of(new Review()));
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(2L, "CUSTOMER", 1L, 5, "Good product", ""));
        when(reviewDao.findByUserAndProduct(2L, 1L)).thenReturn(Optional.empty()); when(orderDao.hasPurchasedProduct(2L, 1L)).thenReturn(false);
        assertThrows(SecurityException.class, () -> reviewService.createReview(2L, "CUSTOMER", 1L, 5, "Good product", ""));
    }

    @Test
    void updateAndDeleteRequireOwnership() throws SQLException {
        Review own = new Review(); own.setId(8L); own.setUserId(2L);
        when(reviewDao.findById(8L)).thenReturn(Optional.of(own)); when(reviewDao.updateReview(8L, 2L, 4, "Updated review")).thenReturn(true); when(reviewDao.deleteReview(8L, 2L)).thenReturn(true);
        assertTrue(reviewService.updateReview(2L, "CUSTOMER", 8L, 4, "Updated review")); assertTrue(reviewService.deleteReview(2L, "CUSTOMER", 8L));
        own.setUserId(9L);
        assertThrows(SecurityException.class, () -> reviewService.updateReview(2L, "CUSTOMER", 8L, 4, "Updated review"));
        assertThrows(SecurityException.class, () -> reviewService.deleteReview(2L, "CUSTOMER", 8L));
    }

    @Test
    void aggregationDelegatesToDatabaseDao() {
        when(reviewDao.getAverageRating(1L)).thenReturn(4.25); when(reviewDao.getReviewCount(1L)).thenReturn(4L);
        assertEquals(4.25, reviewService.getAverageRating(1L)); assertEquals(4L, reviewService.getReviewCount(1L));
    }

}
