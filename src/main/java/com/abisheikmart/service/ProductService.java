package com.abisheikmart.service;

import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDao productDao;

    public ProductService() {
        this.productDao = new ProductDao();
    }

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productDao.findById(id);
    }

    public List<Product> getProductsBySellerId(Long sellerId) {
        return productDao.findBySellerId(sellerId);
    }

    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productDao.findByCategoryId(categoryId);
    }

    public List<Product> searchProducts(String query, String categoryName, String sortBy) {
        return productDao.search(query, categoryName, sortBy);
    }

    public Product createProduct(Long sellerId, ProductRequest req) throws IllegalArgumentException, SQLException {
        validateProductRequest(req);

        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(req.getCategoryId() != null ? req.getCategoryId() : 1L);
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setImageUrl(req.getImageUrl() != null && !req.getImageUrl().isBlank() ? req.getImageUrl().trim() : "images/keyboard.jpg");

        Product saved = productDao.save(p);
        logger.info("Successfully created product id: {}, name: {}, sellerId: {}", saved.getId(), saved.getName(), sellerId);
        return saved;
    }

    public Product updateProduct(Long sellerId, boolean isAdmin, ProductRequest req) throws IllegalArgumentException, SQLException {
        if (req.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for update.");
        }
        validateProductRequest(req);

        Optional<Product> existingOpt = productDao.findById(req.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + req.getId());
        }

        Product existing = existingOpt.get();
        if (!isAdmin && !existing.getSellerId().equals(sellerId)) {
            throw new SecurityException("Unauthorized: You do not own this product listing.");
        }

        existing.setCategoryId(req.getCategoryId());
        existing.setName(req.getName().trim());
        existing.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
        existing.setPrice(req.getPrice());
        existing.setStock(req.getStock());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            existing.setImageUrl(req.getImageUrl().trim());
        }

        productDao.update(existing);
        logger.info("Successfully updated product id: {}", existing.getId());
        return existing;
    }

    public boolean deleteProduct(Long sellerId, boolean isAdmin, Long productId) throws SQLException {
        Optional<Product> existingOpt = productDao.findById(productId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Product existing = existingOpt.get();
        if (!isAdmin && !existing.getSellerId().equals(sellerId)) {
            throw new SecurityException("Unauthorized: You do not own this product listing.");
        }

        boolean deleted = productDao.delete(productId, isAdmin ? null : sellerId);
        logger.info("Deleted product id: {}, result: {}", productId, deleted);
        return deleted;
    }

    private void validateProductRequest(ProductRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (req.getPrice() == null || req.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0.");
        }
        if (req.getStock() == null || req.getStock() < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than or equal to 0.");
        }
    }
}

