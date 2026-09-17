package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.model.SellerOrderSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SellerServiceTest {
    private ProductDao productDao;
    private ProductService productService;
    private OrderDao orderDao;
    private SellerService sellerService;

    @BeforeEach
    void setUp() {
        productDao = mock(ProductDao.class);
        productService = mock(ProductService.class);
        orderDao = mock(OrderDao.class);
        sellerService = new SellerService(productDao, productService, orderDao);
    }

    @Test
    void nonSellerCannotAccessSellerService() {
        assertThrows(SecurityException.class, () -> sellerService.getProducts(1L, "ADMIN"));
        assertThrows(SecurityException.class, () -> sellerService.getDashboard(1L, "CUSTOMER"));
        verifyNoInteractions(productDao, productService, orderDao);
    }

    @Test
    void dashboardUsesSellerScopedDatabaseMetrics() {
        when(productDao.countBySellerId(1L)).thenReturn(4L);
        when(productDao.countActiveBySellerId(1L)).thenReturn(4L);
        when(orderDao.findSellerOrderSummaries(1L)).thenReturn(List.of());
        when(orderDao.countSellerOrders(1L)).thenReturn(3L);
        when(orderDao.countPendingSellerOrders(1L)).thenReturn(1L);

        var stats = sellerService.getDashboard(1L, "SELLER");

        assertEquals(4L, stats.getTotalProducts());
        assertEquals(4L, stats.getActiveProducts());
        assertEquals(3L, stats.getTotalOrders());
        assertEquals(1L, stats.getPendingOrders());
    }

    @Test
    void statusTransitionRequiresSellerOwnershipAndValidNextState() {
        SellerOrderSummary line = new SellerOrderSummary();
        line.setStatus("PLACED");
        when(orderDao.findSellerOrderSummariesByOrderId(10L, 1L)).thenReturn(List.of(line));
        when(orderDao.updateSellerOrderStatus(10L, 1L, "SHIPPED")).thenReturn(true);

        assertTrue(sellerService.updateOrderStatus(1L, "SELLER", 10L, "SHIPPED"));
        verify(orderDao).updateSellerOrderStatus(10L, 1L, "SHIPPED");

        assertThrows(IllegalArgumentException.class, () -> sellerService.updateOrderStatus(1L, "SELLER", 10L, "DELIVERED"));
    }
}
