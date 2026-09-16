package com.abisheikmart.service;

import com.abisheikmart.dao.ReviewDao;
import com.abisheikmart.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewDao reviewDao;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewDao = mock(ReviewDao.class);
        reviewService = new ReviewService(reviewDao);
    }

    @Test
    @DisplayName("Adding review with valid rating (1-5) should save successfully")
    void testAddReviewSuccess() throws SQLException {
        when(reviewDao.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        Review saved = reviewService.addReview(1L, 2L, "Jane Doe", 5, "Amazing product in INR!", "images/keyboard.jpg");

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals(5, saved.getRating());
        assertEquals("Amazing product in INR!", saved.getComment());
    }

    @Test
    @DisplayName("Adding review with invalid rating should throw IllegalArgumentException")
    void testAddReviewInvalidRating() {
        assertThrows(IllegalArgumentException.class, () ->
            reviewService.addReview(1L, 2L, "Jane Doe", 6, "Rating 6 is invalid", "")
        );
    }
}
