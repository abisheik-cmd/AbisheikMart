package com.abisheikmart.controller;

import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.User;
import com.abisheikmart.service.ProductService;
import com.abisheikmart.util.CryptoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ProductController {

    private final ProductService productService;
    private final CryptoUtils cryptoUtils;

    public ProductController(ProductService productService, CryptoUtils cryptoUtils) {
        this.productService = productService;
        this.cryptoUtils = cryptoUtils;
    }

    @GetMapping("/api/products")
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(Map.of("products", products));
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product p = productService.getProductById(id);
            return ResponseEntity.ok(Map.of("product", p));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/seller/products")
    public ResponseEntity<?> getSellerProducts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            List<Product> products = productService.getSellerProducts(user);
            return ResponseEntity.ok(Map.of("products", products));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/seller/products")
    public ResponseEntity<?> createProduct(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestBody ProductRequest req) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            Product p = productService.createProduct(user, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Product created successfully", "product", p));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/seller/products/{id}")
    public ResponseEntity<?> updateProduct(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @PathVariable Long id,
                                           @RequestBody ProductRequest req) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            Product p = productService.updateProduct(user, id, req);
            return ResponseEntity.ok(Map.of("message", "Product updated successfully", "product", p));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/seller/products/{id}")
    public ResponseEntity<?> deleteProduct(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @PathVariable Long id) {
        User user = cryptoUtils.getUserByToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            productService.deleteProduct(user, id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

