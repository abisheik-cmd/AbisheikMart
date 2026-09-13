package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Review;
import com.abisheikmart.model.User;
import com.abisheikmart.service.ReviewService;
import com.abisheikmart.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/review/add")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("You must be logged in to submit a review.")));
            return;
        }

        try {
            JsonObject json = JsonUtil.fromJson(req.getReader(), JsonObject.class);
            Long productId = json.has("productId") ? json.get("productId").getAsLong() : null;
            int rating = json.has("rating") ? json.get("rating").getAsInt() : 5;
            String comment = json.has("comment") ? json.get("comment").getAsString() : "";
            String imageUrl = json.has("imageUrl") ? json.get("imageUrl").getAsString() : "";

            Review review = reviewService.addReview(productId, user.getId(), user.getName(), rating, comment, imageUrl);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Review submitted successfully!", review)));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Error submitting review: " + e.getMessage())));
        }
    }
}
