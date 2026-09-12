package com.abisheikmart.service;

import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductDao productDao;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productDao = mock(ProductDao.class);
        productService = new ProductService(productDao);
    }

    @Test
    @DisplayName("Create product with valid data should call DAO save")
    void testCreateProductSuccess() throws SQLException {
        ProductRequest req = new ProductRequest();
        req.setName("Test Smartwatch");
        req.setDescription("AMOLED display smartwatch");
        req.setPrice(4999.00);
        req.setStock(10);
        req.setCategoryId(1L);

        when(productDao.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        Product created = productService.createProduct(5L, req);

        assertNotNull(created);
        assertEquals(50L, created.getId());
        assertEquals("Test Smartwatch", created.getName());
        assertEquals(4999.00, created.getPrice());
        verify(productDao, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Create product with negative price should throw IllegalArgumentException")
    void testCreateProductInvalidPrice() {
        ProductRequest req = new ProductRequest();
        req.setName("Invalid Product");
        req.setPrice(-100.00);
        req.setStock(5);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(5L, req));
    }

    @Test
    @DisplayName("Deleting product owned by another seller should throw SecurityException")
    void testDeleteProductUnauthorized() {
        Product existing = new Product();
        existing.setId(10L);
        existing.setSellerId(99L); // Owned by seller 99

        when(productDao.findById(10L)).thenReturn(Optional.of(existing));

        // Seller 5 attempts to delete seller 99's product
        assertThrows(SecurityException.class, () -> productService.deleteProduct(5L, false, 10L));
    }
}

