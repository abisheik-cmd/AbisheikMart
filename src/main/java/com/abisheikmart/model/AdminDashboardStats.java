package com.abisheikmart.model;

import java.io.Serializable;

public class AdminDashboardStats implements Serializable {
    private final long totalUsers;
    private final long activeUsers;
    private final long buyers;
    private final long sellers;
    private final long totalProducts;
    private final long activeProducts;
    private final long inactiveProducts;
    private final long totalOrders;
    private final long pendingOrders;
    private final long deliveredOrders;
    private final double marketplaceOrderValue;

    public AdminDashboardStats(long totalUsers, long activeUsers, long buyers, long sellers,
                               long totalProducts, long activeProducts, long inactiveProducts,
                               long totalOrders, long pendingOrders, long deliveredOrders,
                               double marketplaceOrderValue) {
        this.totalUsers = totalUsers; this.activeUsers = activeUsers; this.buyers = buyers; this.sellers = sellers;
        this.totalProducts = totalProducts; this.activeProducts = activeProducts; this.inactiveProducts = inactiveProducts;
        this.totalOrders = totalOrders; this.pendingOrders = pendingOrders; this.deliveredOrders = deliveredOrders;
        this.marketplaceOrderValue = marketplaceOrderValue;
    }
    public long getTotalUsers() { return totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public long getBuyers() { return buyers; }
    public long getSellers() { return sellers; }
    public long getTotalProducts() { return totalProducts; }
    public long getActiveProducts() { return activeProducts; }
    public long getInactiveProducts() { return inactiveProducts; }
    public long getTotalOrders() { return totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public long getDeliveredOrders() { return deliveredOrders; }
    public double getMarketplaceOrderValue() { return marketplaceOrderValue; }
}
