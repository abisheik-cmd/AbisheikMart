package com.abisheikmart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItemRequest {
    @JsonProperty("product_id")
    private Long productId;
    
    private Integer quantity;

    public CartItemRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

