package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Notification;
import com.abisheikmart.model.User;
import com.abisheikmart.service.NotificationService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/notifications/*")
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("No notifications", List.of())));
            return;
        }

        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Fetched user notifications", notifications)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Login required")));
            return;
        }

        String pathInfo = req.getPathInfo();
        if ("/read".equalsIgnoreCase(pathInfo)) {
            notificationService.markRead(user.getId());
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Notifications marked as read", null)));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Notification Endpoint Not Found")));
        }
    }
}
