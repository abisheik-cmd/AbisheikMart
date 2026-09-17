package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.SellerDashboardStats;
import com.abisheikmart.model.SellerOrderSummary;

import java.sql.SQLException;
import java.util.List;

public class SellerService {
    private final ProductDao productDao;
    private final ProductService productService;
    private final OrderDao orderDao;

    public SellerService(ProductDao productDao, ProductService productService, OrderDao orderDao) {
        this.productDao = productDao;
        this.productService = productService;
        this.orderDao = orderDao;
    }

    public void requireSeller(Long sellerId, String role) {
        if (sellerId == null || sellerId <= 0 || !"SELLER".equalsIgnoreCase(role)) {
            throw new SecurityException("SELLER authentication is required.");
        }
    }

    public SellerDashboardStats getDashboard(Long sellerId, String role) {
        requireSeller(sellerId, role);
        long total = productDao.countBySellerId(sellerId);
        long active = productDao.countActiveBySellerId(sellerId);
        List<SellerOrderSummary> orders = orderDao.findSellerOrderSummaries(sellerId);
        List<SellerOrderSummary> recent = orders.size() > 5 ? orders.subList(0, 5) : orders;
        return new SellerDashboardStats(total, active, total - active, orderDao.countSellerOrders(sellerId), orderDao.countPendingSellerOrders(sellerId), recent);
    }

    public List<Product> getProducts(Long sellerId, String role) {
        requireSeller(sellerId, role);
        return productDao.findBySellerId(sellerId);
    }

    public Product createProduct(Long sellerId, String role, ProductRequest request) throws SQLException {
        requireSeller(sellerId, role);
        return productService.createProduct(sellerId, request);
    }

    public Product updateProduct(Long sellerId, String role, ProductRequest request) throws SQLException {
        requireSeller(sellerId, role);
        return productService.updateProduct(sellerId, false, request);
    }

    public boolean deleteProduct(Long sellerId, String role, Long productId) throws SQLException {
        requireSeller(sellerId, role);
        return productService.deleteProduct(sellerId, false, productId);
    }

    public List<SellerOrderSummary> getOrders(Long sellerId, String role) {
        requireSeller(sellerId, role);
        return orderDao.findSellerOrderSummaries(sellerId);
    }

    public boolean updateOrderStatus(Long sellerId, String role, Long orderId, String requestedStatus) {
        requireSeller(sellerId, role);
        if (requestedStatus == null || !List.of("PLACED", "SHIPPED", "DELIVERED").contains(requestedStatus)) {
            throw new IllegalArgumentException("Unsupported seller order status.");
        }
        List<SellerOrderSummary> sellerLines = orderDao.findSellerOrderSummariesByOrderId(orderId, sellerId);
        if (sellerLines.isEmpty()) throw new SecurityException("This order does not contain one of your products.");
        String current = sellerLines.get(0).getStatus();
        boolean valid = ("PLACED".equals(current) && "SHIPPED".equals(requestedStatus))
                || ("SHIPPED".equals(current) && "DELIVERED".equals(requestedStatus));
        if (!valid) throw new IllegalArgumentException("Invalid seller order status transition.");
        if (!orderDao.updateSellerOrderStatus(orderId, sellerId, requestedStatus)) {
            throw new SecurityException("This order is not associated with your products.");
        }
        return true;
    }
}
