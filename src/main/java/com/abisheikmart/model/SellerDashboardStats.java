package com.abisheikmart.model;

import java.io.Serializable;
import java.util.List;

public class SellerDashboardStats implements Serializable {
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long totalOrders;
    private long pendingOrders;
    private List<SellerOrderSummary> recentOrders;

    public SellerDashboardStats(long totalProducts, long activeProducts, long inactiveProducts,
                                long totalOrders, long pendingOrders, List<SellerOrderSummary> recentOrders) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.inactiveProducts = inactiveProducts;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.recentOrders = recentOrders;
    }

    public long getTotalProducts() { return totalProducts; }
    public long getActiveProducts() { return activeProducts; }
    public long getInactiveProducts() { return inactiveProducts; }
    public long getTotalOrders() { return totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public List<SellerOrderSummary> getRecentOrders() { return recentOrders; }
}
