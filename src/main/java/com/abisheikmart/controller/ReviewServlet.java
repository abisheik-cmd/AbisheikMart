package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Review;
import com.abisheikmart.model.User;
import com.abisheikmart.service.ReviewService;
import com.abisheikmart.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/review/add", "/api/review/update", "/api/review/delete", "/reviews/create", "/reviews/update", "/reviews/delete"})
public class ReviewServlet extends HttpServlet {
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json"); resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in."); return; }
        try {
            JsonObject json = JsonUtil.fromJson(req.getReader(), JsonObject.class);
            String path = req.getServletPath();
            if (path.endsWith("/add") || path.endsWith("/create")) {
                Review review = reviewService.createReview(user.getId(), user.getRole(), requiredLong(json, "productId"), requiredInt(json, "rating"), requiredString(json, "comment"), optionalString(json, "imageUrl"));
                resp.setStatus(HttpServletResponse.SC_CREATED); resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Review submitted successfully!", review)));
            } else if (path.endsWith("/update")) {
                boolean updated = reviewService.updateReview(user.getId(), user.getRole(), requiredLong(json, "reviewId"), requiredInt(json, "rating"), requiredString(json, "comment"));
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(updated ? "Review updated successfully!" : "Review was not updated.", null)));
            } else if (path.endsWith("/delete")) {
                boolean deleted = reviewService.deleteReview(user.getId(), user.getRole(), requiredLong(json, "reviewId"));
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(deleted ? "Review deleted successfully!" : "Review was not deleted.", null)));
            } else writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown review operation.");
        } catch (SecurityException e) { writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage()); }
        catch (Exception e) { writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage()); }
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException { resp.setStatus(status); resp.getWriter().write(JsonUtil.toJson(ApiResponse.error(message == null ? "Review request failed." : message))); }
    private Long requiredLong(JsonObject json, String key) { if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException(key + " is required."); return json.get(key).getAsLong(); }
    private int requiredInt(JsonObject json, String key) { return requiredLong(json, key).intValue(); }
    private String requiredString(JsonObject json, String key) { if (!json.has(key) || json.get(key).isJsonNull()) throw new IllegalArgumentException(key + " is required."); return json.get(key).getAsString(); }
    private String optionalString(JsonObject json, String key) { return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : ""; }
}
