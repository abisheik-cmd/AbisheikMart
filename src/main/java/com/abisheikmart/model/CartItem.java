package com.abisheikmart.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItem {
    private Long id;
    
    @JsonProperty("buyer_id")
    private Long buyerId;
    
    @JsonProperty("product_id")
    private Long productId;
    
    @JsonProperty("product_name")
    private String productName;
    
    private String description;
    
    @JsonProperty("unit_price")
    private Double unitPrice;
    
    private Double subtotal;
    private Integer quantity;
    
    @JsonProperty("available_stock")
    private Integer availableStock;
    
    private String category;
    
    @JsonProperty("seller_id")
    private Long sellerId;
    
    @JsonProperty("seller_name")
    private String sellerName;
    
    @JsonProperty("created_at")
    private String createdAt;

    public CartItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

