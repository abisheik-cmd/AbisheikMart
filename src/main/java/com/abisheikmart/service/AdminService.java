package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dao.UserDao;
import com.abisheikmart.model.AdminDashboardStats;
import com.abisheikmart.model.AdminUserSummary;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {
    private static final Set<String> ORDER_STATUSES = Set.of("PLACED", "SHIPPED", "DELIVERED", "CANCELLED");
    private final UserDao userDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;

    public AdminService() { this(new UserDao(), new ProductDao(), new OrderDao()); }

    public AdminService(UserDao userDao, ProductDao productDao, OrderDao orderDao) {
        this.userDao = userDao; this.productDao = productDao; this.orderDao = orderDao;
    }

    public void requireAdmin(Long sessionUserId, String sessionRole) {
        if (sessionUserId == null || sessionUserId <= 0 || !"ADMIN".equalsIgnoreCase(sessionRole)) {
            throw new SecurityException("ADMIN access is required.");
        }
    }

    public AdminDashboardStats getDashboard(Long adminId, String role) {
        requireAdmin(adminId, role);
        return new AdminDashboardStats(userDao.countUsers(), userDao.countActiveUsers(), userDao.countByRole("CUSTOMER"), userDao.countByRole("SELLER"),
                productDao.countProducts(), productDao.countActiveProducts(), productDao.countInactiveProducts(), orderDao.countOrders(),
                orderDao.countOrdersByStatus("PLACED"), orderDao.countOrdersByStatus("DELIVERED"), orderDao.calculateMarketplaceOrderValue());
    }

    public List<AdminUserSummary> listUsers(Long adminId, String role, String search, String userRole, Boolean active) {
        requireAdmin(adminId, role);
        return userDao.findAllUsers(search, userRole, active).stream().map(this::toSummary).collect(Collectors.toList());
    }

    public AdminUserSummary getUser(Long adminId, String role, Long userId) {
        requireAdmin(adminId, role);
        return userDao.findById(userId).map(this::toSummary).orElse(null);
    }

    public boolean updateUserStatus(Long adminId, String role, Long targetUserId, boolean active) throws SQLException {
        requireAdmin(adminId, role);
        if (targetUserId == null || targetUserId.equals(adminId)) throw new SecurityException("An administrator cannot change their own status.");
        return userDao.updateUserStatus(targetUserId, active);
    }

    public List<Product> listProducts(Long adminId, String role, String search, Long categoryId, Long sellerId, Boolean active) {
        requireAdmin(adminId, role);
        return productDao.findAdminProducts(search, categoryId, sellerId, active);
    }

    public Product getProduct(Long adminId, String role, Long productId) {
        requireAdmin(adminId, role);
        return productDao.findById(productId).orElse(null);
    }

    public boolean updateProductStatus(Long adminId, String role, Long productId, boolean active) throws SQLException {
        requireAdmin(adminId, role);
        return productDao.updateProductStatus(productId, active);
    }

    public List<Order> listOrders(Long adminId, String role, String search, String status, Long buyerId) {
        requireAdmin(adminId, role);
        validateStatusFilter(status);
        return orderDao.findAllOrders(search, status, buyerId);
    }

    public Optional<Order> getOrder(Long adminId, String role, Long orderId) {
        requireAdmin(adminId, role);
        return orderDao.findOrderById(orderId);
    }

    public boolean updateOrderStatus(Long adminId, String role, Long orderId, String status) throws SQLException {
        requireAdmin(adminId, role);
        String normalized = validateStatusFilter(status);
        return orderDao.updateOrderStatus(orderId, normalized);
    }

    private String validateStatusFilter(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return "ALL";
        String normalized = status.toUpperCase(Locale.ROOT);
        if (!ORDER_STATUSES.contains(normalized)) throw new IllegalArgumentException("Unsupported order status.");
        return normalized;
    }

    private AdminUserSummary toSummary(User user) {
        return new AdminUserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getActive(), user.getCreatedAt());
    }
}
