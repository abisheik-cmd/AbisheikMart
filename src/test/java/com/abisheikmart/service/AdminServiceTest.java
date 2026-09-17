package com.abisheikmart.service;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dao.UserDao;
import com.abisheikmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {
    private UserDao userDao;
    private ProductDao productDao;
    private OrderDao orderDao;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        productDao = mock(ProductDao.class);
        orderDao = mock(OrderDao.class);
        adminService = new AdminService(userDao, productDao, orderDao);
    }

    @Test
    void onlyAdminCanAccessAdministrativeOperations() {
        assertThrows(SecurityException.class, () -> adminService.getDashboard(1L, "CUSTOMER"));
        assertThrows(SecurityException.class, () -> adminService.getDashboard(1L, "SELLER"));
        assertThrows(SecurityException.class, () -> adminService.getDashboard(null, null));
        verifyNoInteractions(userDao, productDao, orderDao);
    }

    @Test
    void dashboardUsesDatabaseBackedMetrics() {
        when(userDao.countUsers()).thenReturn(5L); when(userDao.countActiveUsers()).thenReturn(4L);
        when(userDao.countByRole("CUSTOMER")).thenReturn(3L); when(userDao.countByRole("SELLER")).thenReturn(1L);
        when(productDao.countProducts()).thenReturn(8L); when(productDao.countActiveProducts()).thenReturn(7L); when(productDao.countInactiveProducts()).thenReturn(1L);
        when(orderDao.countOrders()).thenReturn(10L); when(orderDao.countOrdersByStatus("PLACED")).thenReturn(2L); when(orderDao.countOrdersByStatus("DELIVERED")).thenReturn(6L); when(orderDao.calculateMarketplaceOrderValue()).thenReturn(12345.50);

        var stats = adminService.getDashboard(99L, "ADMIN");

        assertEquals(5L, stats.getTotalUsers()); assertEquals(4L, stats.getActiveUsers()); assertEquals(3L, stats.getBuyers());
        assertEquals(1L, stats.getSellers()); assertEquals(8L, stats.getTotalProducts()); assertEquals(10L, stats.getTotalOrders());
        assertEquals(12345.50, stats.getMarketplaceOrderValue());
    }

    @Test
    void userListingMapsOnlySafeAdministrativeFields() {
        User user = new User(); user.setId(2L); user.setName("Seller"); user.setEmail("seller@test"); user.setRole("SELLER"); user.setActive(true); user.setPasswordHash("secret-hash");
        when(userDao.findAllUsers("sell", "SELLER", true)).thenReturn(List.of(user));
        var results = adminService.listUsers(99L, "ADMIN", "sell", "SELLER", true);
        assertEquals(1, results.size()); assertEquals("seller@test", results.get(0).getEmail()); assertTrue(results.get(0).isActive());
        verify(userDao).findAllUsers("sell", "SELLER", true);
    }

    @Test
    void invalidOrderStatusIsRejectedAndUserStatusDelegates() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> adminService.updateOrderStatus(99L, "ADMIN", 10L, "UNKNOWN"));
        when(userDao.updateUserStatus(2L, false)).thenReturn(true);
        assertTrue(adminService.updateUserStatus(99L, "ADMIN", 2L, false));
        verify(userDao).updateUserStatus(2L, false);
    }

    @Test
    void adminCannotDeactivateOwnAccount() {
        assertThrows(SecurityException.class, () -> adminService.updateUserStatus(99L, "ADMIN", 99L, false));
    }
}
