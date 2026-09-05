package com.abisheikmart.service;

import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.User;
import com.abisheikmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<Product> getSellerProducts(User user) {
        if (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized: Seller access required");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return productRepository.findAll();
        }
        return productRepository.findBySellerId(user.getId());
    }

    public Product createProduct(User user, ProductRequest req) {
        if (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized: Seller access required");
        }
        if (req.getName() == null || req.getName().isBlank() || req.getPrice() == null || req.getPrice() < 0 || req.getStock() == null || req.getStock() < 0) {
            throw new IllegalArgumentException("Product name, valid price, and stock quantity are required");
        }

        Product p = new Product();
        p.setSellerId(user.getId());
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setCategory(req.getCategory() != null ? req.getCategory() : "General");
        p.setImageUrl(req.getImageUrl() != null ? req.getImageUrl() : "");

        return productRepository.save(p);
    }

    public Product updateProduct(User user, Long productId, ProductRequest req) {
        if (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized: Seller access required");
        }

        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !existing.getSellerId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to modify this product");
        }

        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        existing.setPrice(req.getPrice());
        existing.setStock(req.getStock());
        existing.setCategory(req.getCategory());
        existing.setImageUrl(req.getImageUrl());

        productRepository.update(existing);
        return existing;
    }

    public void deleteProduct(User user, Long productId) {
        if (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Unauthorized: Seller access required");
        }
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !existing.getSellerId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to delete this product");
        }

        productRepository.delete(productId, existing.getSellerId());
    }
}

