package com.abisheikmart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Order {
    private Long id;
    
    @JsonProperty("buyer_id")
    private Long buyerId;
    
    @JsonProperty("total_amount")
    private Double totalAmount;
    
    private String status;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    private List<OrderItem> items;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

