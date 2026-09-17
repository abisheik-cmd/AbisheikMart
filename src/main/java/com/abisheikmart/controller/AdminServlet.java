package com.abisheikmart.controller;

import com.abisheikmart.model.User;
import com.abisheikmart.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin", "/admin/", "/admin/users", "/admin/users/view", "/admin/users/status", "/admin/products", "/admin/products/view", "/admin/products/status", "/admin/orders", "/admin/orders/view", "/admin/orders/status"})
public class AdminServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;
        String path = req.getServletPath();
        try {
            if ("/admin".equals(path) || "/admin/".equals(path)) {
                req.setAttribute("stats", adminService.getDashboard(admin.getId(), admin.getRole()));
                req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
            } else if ("/admin/users/view".equals(path)) {
                req.setAttribute("user", adminService.getUser(admin.getId(), admin.getRole(), requiredLong(req.getParameter("id"), "User ID is required.")));
                req.getRequestDispatcher("/WEB-INF/views/admin/user-details.jsp").forward(req, resp);
            } else if ("/admin/users".equals(path)) {
                req.setAttribute("users", adminService.listUsers(admin.getId(), admin.getRole(), req.getParameter("q"), req.getParameter("role"), activeFilter(req.getParameter("active"))));
                req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
            } else if ("/admin/products/view".equals(path)) {
                req.setAttribute("product", adminService.getProduct(admin.getId(), admin.getRole(), requiredLong(req.getParameter("id"), "Product ID is required.")));
                req.getRequestDispatcher("/WEB-INF/views/admin/product-details.jsp").forward(req, resp);
            } else if ("/admin/products".equals(path)) {
                req.setAttribute("products", adminService.listProducts(admin.getId(), admin.getRole(), req.getParameter("q"), optionalLong(req.getParameter("categoryId")), optionalLong(req.getParameter("sellerId")), activeFilter(req.getParameter("active"))));
                req.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(req, resp);
            } else if ("/admin/orders/view".equals(path)) {
                req.setAttribute("order", adminService.getOrder(admin.getId(), admin.getRole(), requiredLong(req.getParameter("id"), "Order ID is required.")).orElse(null));
                req.getRequestDispatcher("/WEB-INF/views/admin/order-details.jsp").forward(req, resp);
            } else if ("/admin/orders".equals(path)) {
                req.setAttribute("orders", adminService.listOrders(admin.getId(), admin.getRole(), req.getParameter("q"), req.getParameter("status"), optionalLong(req.getParameter("buyerId"))));
                req.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(req, resp);
            }
        } catch (RuntimeException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;
        String path = req.getServletPath();
        try {
            if ("/admin/users/status".equals(path)) {
                adminService.updateUserStatus(admin.getId(), admin.getRole(), requiredLong(req.getParameter("userId"), "User ID is required."), parseBooleanStatus(req.getParameter("active")));
                resp.sendRedirect(req.getContextPath() + "/admin/users?updated=success");
            } else if ("/admin/products/status".equals(path)) {
                adminService.updateProductStatus(admin.getId(), admin.getRole(), requiredLong(req.getParameter("productId"), "Product ID is required."), parseBooleanStatus(req.getParameter("active")));
                resp.sendRedirect(req.getContextPath() + "/admin/products?updated=success");
            } else if ("/admin/orders/status".equals(path)) {
                adminService.updateOrderStatus(admin.getId(), admin.getRole(), requiredLong(req.getParameter("orderId"), "Order ID is required."), req.getParameter("status"));
                resp.sendRedirect(req.getContextPath() + "/admin/orders?updated=success");
            }
        } catch (SecurityException exception) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
        } catch (IllegalArgumentException | java.sql.SQLException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private User requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN access is required.");
            return null;
        }
        adminService.requireAdmin(user.getId(), user.getRole());
        return user;
    }

    private Long requiredLong(String value, String message) { if (value == null || value.isBlank()) throw new IllegalArgumentException(message); return Long.valueOf(value); }
    private Long optionalLong(String value) { return value == null || value.isBlank() ? null : Long.valueOf(value); }
    private Boolean activeFilter(String value) { return value == null || value.isBlank() || "ALL".equalsIgnoreCase(value) ? null : parseBooleanStatus(value); }
    private boolean parseBooleanStatus(String value) { if ("true".equalsIgnoreCase(value) || "active".equalsIgnoreCase(value)) return true; if ("false".equalsIgnoreCase(value) || "inactive".equalsIgnoreCase(value)) return false; throw new IllegalArgumentException("Invalid active status."); }
}
