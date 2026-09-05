package com.abisheikmart.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItem {
    private Long id;
    
    @JsonProperty("order_id")
    private Long orderId;
    
    @JsonProperty("product_id")
    private Long productId;
    
    @JsonProperty("seller_id")
    private Long sellerId;
    
    @JsonProperty("product_name")
    private String productName;
    
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private Double unitPrice;
    
    private Double subtotal;

    public OrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}

